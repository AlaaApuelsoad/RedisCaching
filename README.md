Redis Cheat Sheet
1️⃣ Connection & Info
pgsql
Copy
Edit
PING                        # Check connection (returns PONG)
AUTH mypassword             # Authenticate
SELECT 0                    # Switch database (0-15 by default)
INFO                        # Show server info & stats
CONFIG GET *                 # Get all config
2️⃣ Key Management
pgsql
Copy
Edit
KEYS *                      # List all keys (⚠️ slow in production)
EXISTS mykey                # Check if key exists
TYPE mykey                  # Get type of key
TTL mykey                   # Time-to-live in seconds
EXPIRE mykey 60             # Set expiration to 60 sec
PERSIST mykey               # Remove expiration
DEL mykey                   # Delete key
FLUSHDB                     # Delete all keys in current DB
FLUSHALL                    # Delete all keys in all DBs
3️⃣ Strings
vbnet
Copy
Edit
SET mykey "Hello"           # Set value
GET mykey                   # Get value
APPEND mykey " World"       # Append to string
INCR counter                # Increment integer
DECR counter                # Decrement integer
4️⃣ Hashes (Object-like)
pgsql
Copy
Edit
HSET user:1 name "John" age "30"  # Set fields
HGET user:1 name                  # Get single field
HGETALL user:1                    # Get all fields
HDEL user:1 age                   # Delete a field
HEXISTS user:1 name               # Check if field exists
5️⃣ Lists (Queue-like)
sql
Copy
Edit
LPUSH mylist "one" "two"    # Push left
RPUSH mylist "three"        # Push right
LPOP mylist                 # Pop left
RPOP mylist                 # Pop right
LRANGE mylist 0 -1          # Get all elements
6️⃣ Sets (Unique values)
sql
Copy
Edit
SADD myset "a" "b"          # Add members
SMEMBERS myset              # Show all members
SISMEMBER myset "a"         # Check if member exists
SREM myset "a"              # Remove member
7️⃣ Sorted Sets (Ranked)
sql
Copy
Edit
ZADD scores 100 "Alice"     # Add with score
ZRANGE scores 0 -1 WITHSCORES  # Show all members with scores
ZREM scores "Alice"         # Remove member

Reference [Medium article](https://medium.com/vedity/spring-boot-caching-mechanism-8ef901147e60).
