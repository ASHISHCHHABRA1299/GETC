import os
import re
import time
import subprocess
import pyrebase

def get_and_write(code):
    file = open('src.c', 'w')
    if not code:
        return (0, "Empty file")

    inside_quotes = 0
    slashn_indexes = []
    
    for i, character in enumerate(code):
        if character == '"':
            if inside_quotes == 1:
                inside_quotes = 0
            else:
                inside_quotes = 1
        if inside_quotes:
            if character == '\n':
                slashn_indexes.append(i)
    
    factor = 0
    for i in slashn_indexes:
        i = i+factor
        code = code[:i] + '\\n' + code[i+1:]
        factor+=1
    
    file.write(code)
    file.close()

    # Compiling and executing
    result = os.system('gcc src.c')
    if result == 0:
        proc = subprocess.Popen(["./a.out"], stdout=subprocess.PIPE)
        out = proc.communicate()[0]
    else:
        out = "Syntax Error"
    #print("Result: {}".format(out.upper().decode('utf-8')))

    if not isinstance(out, str):
        out = out.decode('utf-8')

    return (result, out)

if __name__ == "__main__":
    config = { 
        "apiKey": "",                            #specify yours
        "authDomain": "", 
        "databaseURL": "", 
        "storageBucket": "" 
    } 
    firebase = pyrebase.initialize_app(config)
    db = firebase.database()
    new_code = db.child('text').get().val()
    code = ""
    print("We're on")
    count = 0
    try:
        while True:
            if new_code and new_code != code:
                print("Code changed, compiling!")
                code = new_code
                result, output = get_and_write(new_code.lower()) 
                data = {'result': result, 'output': output}
                db.set(data)
                count=0
            else:
                count+=1
                print("Waiting" + ('.'*count).ljust(10,' ') , end='\r')

            if count==10:
                count=0

            new_code = db.child('text').get().val()
    except KeyboardInterrupt:
        print("Exiting")

