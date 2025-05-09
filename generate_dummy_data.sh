# sleep 10

# # Admin credentials
# PB_ADMIN_EMAIL="admin@aktiv.com"
# PB_ADMIN_PASSWORD="qwerty123"

# # Log in to get token
# LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8090/api/admins/auth-with-password \
#   -H "Content-Type: application/json" \
#   -d "{\"identity\":\"$PB_ADMIN_EMAIL\",\"password\":\"$PB_ADMIN_PASSWORD\"}")

# PB_TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*' | grep -o '[^"]*$')

# # Insert dummy workouts
# PB_URL="http://localhost:8090/api/collections/workouts/records"

# curl -X POST $PB_URL \
#   -H "Content-Type: application/json" \
#   -H "Authorization: Bearer $PB_TOKEN" \
#   -d '{"name": "Curls", "difficulty": "Easy"}'

# curl -X POST $PB_URL \
#   -H "Content-Type: application/json" \
#   -H "Authorization: Bearer $PB_TOKEN" \
#   -d '{"name": "Bench press", "difficulty": "Medium"}'

# curl -X POST $PB_URL \
#   -H "Content-Type: application/json" \
#   -H "Authorization: Bearer $PB_TOKEN" \
#   -d '{"name": "Squats", "difficulty": "Hard"}'
