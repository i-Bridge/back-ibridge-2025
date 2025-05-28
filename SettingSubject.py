#!/usr/bin/python3

import requests

SERVER_URL = "http://localhost:8080/back/setSubject"

def send_request():
    try:
        return requests.get(SERVER_URL)
    except requests.exceptions.RequestException as e:
        print(f"Error sending request: {e}")
        return None

if __name__ == "__main__":
    response = send_request()
    if response is not None:
        if response.status_code == 200:
            print("✅ Request was successful.")
            print("Response body:", response.text)
        elif response.status_code == 404:
            print("❌ Not Found (404)")
        else:
            print(f"⚠️ Request failed with status code: {response.status_code}")
            print(f"Response body: {response.text}")
    else:
        print("❌ No response received.")
