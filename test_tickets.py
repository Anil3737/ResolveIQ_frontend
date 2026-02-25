import requests
import json

BASE_URL = "http://127.0.0.1:5000/api"

def test_tickets():
    # Login as admin to get token
    login_url = f"{BASE_URL}/auth/login"
    login_payload = {"email": "admin@company.com", "password": "password"}
    login_response = requests.post(login_url, json=login_payload)
    
    if login_response.status_code == 200:
        token = login_response.json()['data']['access_token']
        headers = {"Authorization": f"Bearer {token}"}
        
        # Get tickets
        tickets_url = f"{BASE_URL}/admin/tickets"
        response = requests.get(tickets_url, headers=headers)
        
        print(f"Fetch tickets status: {response.status_code}")
        if response.status_code == 200:
            print(f"Fetched {len(response.json()['data'])} tickets successfully.")
        else:
            print(f"❌ Error fetching tickets: {response.text}")
    else:
        print(f"❌ Login failed: {login_response.text}")

if __name__ == "__main__":
    test_tickets()
