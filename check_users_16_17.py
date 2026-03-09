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

def check_user():
    try:
        connection = pymysql.connect(**DB_CONFIG)
        with connection.cursor() as cursor:
            print("\n--- USER 16 & 17 ---")
            cursor.execute("SELECT id, full_name, role_id, email FROM users WHERE id IN (16, 17)")
            for row in cursor.fetchall():
                print(f"ID: {row['id']} | Name: {row['full_name']} | Email: {row['email']}")
                
            print("\n--- TEAM LEAD PROFILES ---")
            cursor.execute("SELECT user_id, department_id FROM team_lead_profiles WHERE user_id IN (16, 17)")
            for row in cursor.fetchall():
                print(f"User ID: {row['user_id']} | Dept ID: {row['department_id']}")
                
    except Exception as e:
        print(f"Error: {e}")
    finally:
        if 'connection' in locals():
            connection.close()

if __name__ == "__main__":
    check_user()
