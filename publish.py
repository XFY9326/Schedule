#!/usr/bin/env python3
import os
import shutil
import zipfile
import json
import requests
from requests.auth import HTTPBasicAuth
from typing import Optional, Dict

UPLOAD_APK = True
ARCHIVE_APK = True

project_name = "PureSchedule"
release_dir = "app/release"
release_config = f"{release_dir}/output-metadata.json"
release_mapping_dir = "app/build/outputs/mapping/release"
product_name = "tool.xfy9326.schedule.apk"
publish_url = f"https://XFY9326-generic.pkg.coding.net/Schedule/release/{product_name}?version=%d"

def read_properties(file_path: str) -> Dict[str, str]:
    result = {}
    with open(file_path, "r", encoding="UTF-8") as f:
        line = f.readline()
        while line != '':
            line = line.strip()
            if not line.startswith("#") and "=" in line:
                divider_index = line.index("=")
                key = line[:divider_index].strip()
                value = line[divider_index + 1:].strip()
                result[key] = value
            line = f.readline()
    return result

local_properties = read_properties("local.properties")
publish_user = local_properties["coding.release.username"]
publish_password = local_properties["coding.release.password"]


def loads_config() -> Optional[dict]:
    if os.path.isfile(release_config):
        with open(release_config, "r") as f:
            return json.load(f)
    return None


def upload_file(url: str, file_path: str) -> bool:
    with open(file_path, 'rb') as f:
        file_data = f.read()
    with requests.put(url, data=file_data, auth=HTTPBasicAuth(publish_user, publish_password)) as r:
        return r.status_code == 200


def archive_files(version_name: str, version_code: int, release_file: str, mapping_dir: str, output_dir: str):
    archive_apk_name = f"{project_name}_v{version_name}_{version_code}_release.apk"
    archive_mapping_name = f"{version_name}.zip"
    shutil.copyfile(release_file, os.path.join(output_dir, archive_apk_name))
    abs_mapping_dir = os.path.abspath(os.path.join(os.getcwd(), mapping_dir))
    with zipfile.ZipFile(os.path.join(output_dir, archive_mapping_name), "w", zipfile.ZIP_DEFLATED) as mapping_zip:
        for path, dir_names, file_names in os.walk(abs_mapping_dir):
            relative_zip_path = path.replace(abs_mapping_dir, "")
            relative_zip_path = relative_zip_path and relative_zip_path
            for file_name in file_names:
                mapping_zip.write(os.path.join(abs_mapping_dir, file_name), os.path.join(relative_zip_path, file_name))


def main():
    print("Coding Release Publish")

    config = loads_config()
    if config is None:
        print(f"No release config in [{release_config}]!")
        return

    try:
        apk_info = config["elements"][0]
        version_code = int(apk_info["versionCode"])
        version_name = apk_info["versionName"]
        file_name = apk_info["outputFile"]
    except:
        print(f"Config read error!")
        return

    release_file = f"{release_dir}/{file_name}"
    if not os.path.isfile(release_file):
        print(f"No release file in '{release_file}'!")
        return
    if not os.path.isdir(release_mapping_dir):
        print(f"No mapping file in '{release_mapping_dir}'!")
        return

    print(f"Version: {version_code}\nFile: {file_name}")

    if UPLOAD_APK:
        try:
            if upload_file(publish_url % version_code, release_file):
                print("Upload success")
            else:
                print("Upload failed!")
        except BaseException as e:
            print(f"Upload error! Msg: {e}")
    
    if ARCHIVE_APK:
        try:
            archive_files(version_name, version_code, release_file, release_mapping_dir, release_dir)
            print("Archive success")
        except BaseException as e:
            print(f"Archive error! Msg: {e}")
    
    


if __name__ == '__main__':
    main()
