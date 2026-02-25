import requests
import json

BASE_URL = "http://127.0.0.1:5000/api"

def test_login(email, password):
    url = f"{BASE_URL}/auth/login"
    payload = {"email": email, "password": password}
    try:
        response = requests.post(url, json=payload)
        print(f"Login attempt for {email}:")
        print(f"Status: {response.status_code}")
        print(f"Body: {response.text}")
    except Exception as e:
        print(f"❌ Connection error: {e}")

if __name__ == "__main__":
    # Test common credentials
    test_login("admin@company.com", "password")
    test_login("admin@resolveiq.com", "password")
    test_login("anil@resolveiq.com", "password")
