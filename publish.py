#!/usr/bin/env python3
import os
import json
import shutil
import zipfile

import requests
import mimetypes
from requests.auth import HTTPBasicAuth

UPLOAD_CODING_ARTIFACT = True
UPLOAD_GITHUB_ARTIFACT = True
ARCHIVE_ARTIFACT = True

PROJECT_NAME = "PureSchedule"
PRODUCT_NAME = "tool.xfy9326.schedule.apk"
VARIANT_NAME = "release"
PROJECT_ROOT = os.path.dirname(os.path.realpath(__file__))
OUTPUT_DIR = os.path.join(PROJECT_ROOT, "app", VARIANT_NAME)
BUILD_MAPPING_DIR = os.path.join(PROJECT_ROOT, "app", "build", "outputs", "mapping", VARIANT_NAME)
OUTPUT_META_PATH = os.path.join(OUTPUT_DIR, "output-metadata.json")
LOCAL_PROPERTIES_PATH = os.path.join(PROJECT_ROOT, "local.properties")

CODING_UPLOAD_URL = f"https://XFY9326-generic.pkg.coding.net/Schedule/release/{PRODUCT_NAME}?version=%d"
GITHUB_API_URL = "https://api.github.com/repos/XFY9326/Schedule"
GITHUB_UPLOAD_URL = "https://uploads.github.com/repos/XFY9326/Schedule/releases/%d/assets?name=%s"


def read_properties(file_path: str) -> dict[str, str]:
    result = {}
    with open(file_path, "r", encoding="UTF-8") as f:
        line = f.readline()
        while line != "":
            line = line.strip()
            if not line.startswith("#") and "=" in line:
                divider_index = line.index("=")
                key = line[:divider_index].strip()
                value = line[divider_index + 1:].strip()
                result[key] = value
            line = f.readline()
    return result


def get_artifact_info() -> dict:
    if os.path.isfile(OUTPUT_META_PATH):
        with open(OUTPUT_META_PATH, "r", encoding="utf-8") as f:
            config = json.load(f)
    else:
        raise FileNotFoundError(f"Load metadata failed in {OUTPUT_META_PATH}!")
    artifact = config["elements"][0]
    return {
        "version_code": int(artifact["versionCode"]),
        "version_name": artifact["versionName"],
        "output_file": os.path.join(OUTPUT_DIR, artifact["outputFile"]),
    }


def checking_files(artifact_file: str):
    if not os.path.isfile(artifact_file):
        raise FileNotFoundError(f"No artifact in '{artifact_file}'!")
    if not os.path.isdir(BUILD_MAPPING_DIR):
        raise FileNotFoundError(f"No mapping dir in '{BUILD_MAPPING_DIR}'!")


def get_content_type(file_path: str) -> str:
    content_type = mimetypes.guess_type(file_path)[0]
    if content_type is not None:
        return content_type
    return "application/octet-stream"


def upload_coding_artifact(user: str, password: str, artifact: dict):
    url = CODING_UPLOAD_URL % artifact["version_code"]
    with requests.head(url) as r:
        not_exist = r.status_code == 404
    if not_exist:
        with open(artifact["output_file"], "rb") as f:
            with requests.put(url, data=f, auth=HTTPBasicAuth(user, password)) as r:
                if r.status_code != 200:
                    raise ConnectionError(f"File '{artifact['output_file']}' upload failed!")
                else:
                    print("Coding upload success!")
    else:
        print(f"Artifact version {artifact['version_code']} already exists!")


def upload_github_artifact(token: str, artifact: dict):
    github_headers = {"Accept": "application/vnd.github+json", "Authorization": f"Bearer {token}"}
    tag_url = GITHUB_API_URL + "/tags"
    with requests.get(tag_url, headers=github_headers) as r:
        if r.status_code == 200:
            version_tag = None
            for tag in r.json():
                if artifact["version_name"] in tag["name"]:
                    version_tag = tag["name"]
                    break
        else:
            raise ConnectionError(f"Unable to connect '{tag_url}' with code {r.status_code}")
    if version_tag is not None:
        assets_not_exist = True
        release_not_exists = True
        release_id = None
        release_tag_url = GITHUB_API_URL + "/releases/tags/" + version_tag
        with requests.get(release_tag_url, headers=github_headers) as r:
            if r.status_code == 200:
                release_info = r.json()
                assets_not_exist = len(release_info["assets"]) == 0
                release_id = release_info["id"]
                release_not_exists = False
            elif r.status_code != 404:
                raise ConnectionError(f"Unable to connect '{release_tag_url}' with code {r.status_code}")
        if release_not_exists:
            create_release_url = GITHUB_API_URL + "/releases"
            create_release_json = {"tag_name": version_tag, "name": version_tag, "generate_release_notes": True}
            with requests.get(create_release_url, json=create_release_json, headers=github_headers) as r:
                if r.status_code == 201:
                    create_release_info = r.json()
                    assets_not_exist = True
                    release_id = create_release_info["id"]
                    release_not_exists = False
                else:
                    raise ConnectionError(f"Unable to connect '{release_tag_url}' with code {r.status_code}")
        if assets_not_exist:
            if release_id is None:
                release_json = {"tag_name": version_tag, "name": version_tag}
                with requests.post(release_tag_url, json=release_json, headers=github_headers) as r:
                    if r.status_code == 201:
                        release_id = r.json["id"]
                    else:
                        raise ConnectionError(f"Release '{version_tag}' create failed with code {r.status_code}")
            with open(artifact["output_file"], "rb") as f:
                upload_url = GITHUB_UPLOAD_URL % (release_id, os.path.basename(artifact["output_file"]))
                upload_headers = github_headers.copy()
                upload_headers["Content-Type"] = get_content_type(artifact["output_file"])
                with requests.post(upload_url, data=f, headers=upload_headers) as r:
                    if r.status_code != 201:
                        raise ConnectionError(f"File '{artifact['output_file']}' upload failed!")
                    else:
                        print("GitHub upload success!")
        else:
            print(f"Artifact version {version_tag} already exists!")
    else:
        raise ValueError(f"Tag for version '{artifact['version_name']}' not exist!")


def archive_artifact(artifact: dict):
    archive_apk_name = f"{PROJECT_NAME}_v{artifact['version_name']}_{artifact['version_code']}_{VARIANT_NAME}.apk"
    archive_apk = os.path.join(OUTPUT_DIR, archive_apk_name)

    archive_mapping_name = f"{artifact['version_name']}.zip"
    archieve_mapping = os.path.abspath(os.path.join(OUTPUT_DIR, archive_mapping_name))

    if os.path.isfile(archive_apk):
        os.remove(archive_apk)
    shutil.copyfile(artifact["output_file"], archive_apk)

    if os.path.isfile(archieve_mapping):
        os.remove(archieve_mapping)
    with zipfile.ZipFile(archieve_mapping, "w", zipfile.ZIP_DEFLATED) as zip:
        for path, _, file_names in os.walk(BUILD_MAPPING_DIR):
            relative_zip_path = path.replace(BUILD_MAPPING_DIR, "")
            relative_zip_path = relative_zip_path and relative_zip_path
            for file_name in file_names:
                zip.write(
                    os.path.join(BUILD_MAPPING_DIR, file_name),
                    os.path.join(relative_zip_path, file_name),
                )


def main():
    print(f"{PROJECT_NAME} Release Publish")

    # Add more mime types
    mimetypes.types_map[".apk"] = "application/vnd.android.package-archive"

    local_properties = read_properties(LOCAL_PROPERTIES_PATH)
    coding_user = local_properties["coding.release.username"]
    coding_password = local_properties["coding.release.password"]
    github_token = local_properties["github.release.token"]

    print(f"Variant: {VARIANT_NAME}")
    artifact = get_artifact_info()
    checking_files(artifact["output_file"])

    print(f"Version: {artifact['version_name']} ({artifact['version_code']})")
    print(f"File: {os.path.basename(artifact['output_file'])}")

    if ARCHIVE_ARTIFACT:
        print("\nPackaging outputs ...")
        try:
            archive_artifact(artifact)
            print("Successfully archived!")
        except BaseException as e:
            print(e)

    if UPLOAD_CODING_ARTIFACT:
        print("\nUploading Coding ...")
        try:
            upload_coding_artifact(coding_user, coding_password, artifact)
        except BaseException as e:
            print(e)

    if UPLOAD_GITHUB_ARTIFACT:
        print("\nUploading GitHub ...")
        try:
            upload_github_artifact(github_token, artifact)
        except BaseException as e:
            print(e)


if __name__ == "__main__":
    main()
