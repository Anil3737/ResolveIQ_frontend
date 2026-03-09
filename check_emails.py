import pymysql
import os
from dotenv import load_dotenv

load_dotenv()

DB_CONFIG = {
    'host': os.getenv('DB_HOST', '127.0.0.1'),
    'user': os.getenv('DB_USER', 'root'),
    'password': os.getenv('DB_PASSWORD', ''),
    'database': os.getenv('DB_NAME', 'resolveiq'),
    'cursorclass': pymysql.cursors.DictCursor
}

def check_emails():
    try:
        connection = pymysql.connect(**DB_CONFIG)
        with connection.cursor() as cursor:
            print("\n--- USERS ---")
            cursor.execute("SELECT id, full_name, email FROM users WHERE full_name LIKE '%Pushpa%' OR full_name LIKE '%Pratap%' OR full_name LIKE '%Gopi%' OR email LIKE '%pushpa%'")
            for row in cursor.fetchall():
                print(f"ID: {row['id']} | Name: {row['full_name']} | Email: {row['email']}")
                
    except Exception as e:
        print(f"Error: {e}")
    finally:
        if 'connection' in locals():
            connection.close()

if __name__ == "__main__":
    check_emails()
