# Much of this code is obtained from the tutorial on how to host a server, as well as the configuratio files
# Link to the tutorial: https://www.digitalocean.com/community/tutorials/how-to-deploy-a-flask-application-on-an-ubuntu-vps
import os
from flask import Flask, request, jsonify, json, url_for, flash, redirect
from flask_cors import CORS
from werkzeug.utils import secure_filename
app = Flask(__name__)
CORS(app)
@app.route("/")
def hello():
    return "Hello, I love Digital Ocean!"
if __name__ == "__main__":
    app.run()

@app.route("/page", methods=['POST', 'GET'])
#UPLOAD_FOLDER = '/path/to/the/uploads'
#ALLOWED_EXTENSIONS = {'txt', 'pdf', 'png', 'jpg', 'jpeg', 'gif'}
#app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

def get_task():
    if request.method == 'POST':
        bits = request.files['12345']
    #    bits = request.form
        return jsonify({"message":bits})
    if request.method == 'GET':
        return jsonify({"message":"this is a get request"})
    return  jsonify({"message":"this is some other request"})
 
if __name__ == "__main__":
    app.run(debug=True)
