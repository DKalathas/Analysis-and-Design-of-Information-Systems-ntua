# importing the requests library
import requests

# defining the api-endpoint
API_ENDPOINT = "http://localhost:15672/api/queues/%2f/webq1/get"

headers = {'content-type': 'application/json'}
#data to be sent to api
pdata = {'count':'5','ackmode':'ack_requeue_false','encoding':'auto','truncate':'50000'}

# sending post request and saving response as response object
r = requests.post(url = API_ENDPOINT ,auth=('admin', 'admin'), json = pdata, headers=headers)

# extracting response text
pastebin_url = r.text
print("Response :%s"%pastebin_url)