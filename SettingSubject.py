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
    if response.code == 200:
        print("Request was successful.")
    else:
        print(f"Request failed with status code: {response.code}")
        print(f"Response message : {response.message}")
