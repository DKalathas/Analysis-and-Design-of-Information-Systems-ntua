# importing the requests library
import requests
import json

import requests

# defining the api-endpoint
API_ENDPOINT = "http://127.0.0.1:5000/"

pdata = {'payload': '90'}
headers = {'content-type': 'application/json'}


r = requests.post(url = API_ENDPOINT, json=pdata)

print(r.text)


# # data to be sent to api

# pdata = {'properties':{},'routing_key':'webq1','payload':'90.4','payload_encoding':'string'}

# # sending post request and saving response as response object
# r = requests.post(url = API_ENDPOINT ,auth=('admin', 'admin'), json = pdata, headers=headers)

# # extracting response text
# pastebin_url = r.text
# print("Response :%s"%pastebin_url)