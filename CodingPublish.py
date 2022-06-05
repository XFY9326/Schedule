#!/usr/bin/env python3
import os
import json
import requests
from requests.auth import HTTPBasicAuth
from typing import Optional, Dict

release_dir = "app/release"
release_config = f"{release_dir}/output-metadata.json"
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


def main():
    print("Coding Release Publish")

    config = loads_config()
    if config is None:
        print(f"No release config in [{release_config}]!")
        return

    try:
        version_code = int(config["elements"][0]["versionCode"])
        file_name = config["elements"][0]["outputFile"]
    except:
        print(f"Config read error!")
        return

    release_file = f"{release_dir}/{file_name}"
    if not os.path.isfile(release_file):
        print(f"No release file in [{release_file}]!")
        return

    print(f"Version: {version_code}\nFile: {file_name}")

    try:
        if upload_file(publish_url % version_code, release_file):
            print("Upload success")
        else:
            print("Upload failed!")
    except BaseException as e:
        print(f"Upload error! Msg: {e}")


if __name__ == '__main__':
    main()
