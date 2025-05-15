import requests

SERVER_URL = "http://10.0.12.38:8080/back/setSubject"

def send_request():
    try:
        response = requests.get(SERVER_URL)
    except requests.exceptions.RequestException as e:
        print(f"Error sending request: {e}")
        return None

if __name__ == "__main__":
    send_request()
    print("Request sent successfully.")