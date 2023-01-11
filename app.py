from flask import Flask, request, jsonify, render_template, url_for, flash, redirect
import requests

app = Flask(__name__)
app.config['SECRET_KEY'] = '88c9056a40c5dbd43fd1eb9af1327d4078e64d6a37239722'

messages = [{'title': 'Message One',
             'content': 'Message One Content'},
            {'title': 'Message Two',
             'content': 'Message Two Content'}
            ]

@app.route('/', methods=['GET'])
def index():
    return render_template('index.html', messages=messages)


@app.route('/create/', methods=('GET', 'POST'))
def create():
    if request.method == 'POST':
        title = request.form['title']
        content = request.form['content']

        if not title:
            flash('Title is required!')
        elif not content:
            flash('Content is required!')
        else:
            messages.append({'title': title, 'content': content})
            return redirect(url_for('index'))

    return render_template('create.html')




@app.route('/api/queue', methods=['GET', 'POST'])
def receive():
    headers = {'content-type': 'application/json'}

    if request.method == 'GET':
        # defining the api-endpoint
        API_ENDPOINT = "http://localhost:15672/api/queues/%2f/webq1/get"

        #data to be sent to api
        pdata = {'count':'2','ackmode':'ack_requeue_false','encoding':'auto','truncate':'50000'}

        # sending post request and saving response as response object
        r = requests.post(url = API_ENDPOINT ,auth=('admin', 'admin'), json = pdata, headers=headers)

        # extracting response text
        pastebin_url = r.text
        print("Response :%s"%pastebin_url)

        return jsonify(pastebin_url)

    if request.method == 'POST':
        # defining the api-endpoint
        API_ENDPOINT = "http://localhost:15672/api/exchanges/%2f/webex/publish"

        # data to be sent to api
        pdata = {'properties':{},'routing_key':'webq1','payload':'90.4','payload_encoding':'string'}

        # sending post request and saving response as response object
        r = requests.post(url = API_ENDPOINT ,auth=('admin', 'admin'), json = pdata, headers=headers)

        # extracting response text
        pastebin_url = r.text
        print("Response :%s"%pastebin_url)

        return jsonify(pastebin_url)
    

if __name__ == '__main__':
    app.run(debug=True)