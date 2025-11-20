import bcrypt

def generate_password_hash(password):
    salt = bcrypt.gensalt()
    hashed = bcrypt.hashpw(password.encode('utf-8'), salt)
    return hashed.decode('utf-8')

if __name__ == "__main__":
    password = input("Ingrese la contraseña a encriptar: ")
    hashed = generate_password_hash(password)
    print("BCrypt hasheado:")
    print(hashed)
