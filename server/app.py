from flask import Flask,render_template, request, redirect, url_for, send_file
import os

app = Flask(__name__)


@app.route('/')
def index():
    return render_template('index.html')

@app.route('/execute')
def index2():
    return render_template('index2.html')

@app.route('/execute',methods=['POST'])
def exec():
    
    os.system('python contactMatrix.py '+request.form['arg1']+' '+ request.form['arg2'])
    return send_file('matrix.txt', attachment_filename='matrix.txt')

@app.route('/', methods=['POST'])
def upload_file():
    uploaded_file = request.files['database']
    if uploaded_file.filename != '':
        uploaded_file.save(uploaded_file.filename)
    return "Success"

if __name__ == '__main__':
	app.run(host='0.0.0.0')
