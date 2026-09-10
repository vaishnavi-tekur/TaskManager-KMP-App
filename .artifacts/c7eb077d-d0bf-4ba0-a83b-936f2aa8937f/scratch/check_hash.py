import hashlib

def hash_sha256(v):
    return hashlib.sha256(v.encode('utf-8')).hexdigest()

password = "Vaishu@00"
print(f"Password: {password}")
print(f"Hash: {hash_sha256(password)}")
