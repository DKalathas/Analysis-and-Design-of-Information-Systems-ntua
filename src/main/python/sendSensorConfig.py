import time
import pika
import random
import sys

key = sys.argv[1]
value = sys.argv[2]
host = sys.argv[3]
port = int(sys.argv[4])
queue_name = sys.argv[5]
routing_key = sys.argv[5]
seconds = float(sys.argv[6])

props = { key : value }

connection = pika.BlockingConnection(pika.ConnectionParameters(host, port=port, client_properties=props))
channel = connection.channel()
print(connection)

channel.queue_declare(queue=queue_name, durable=True)

while(True):
    # Generate random data
    temp = round(random.uniform(18.00, 22.99), 2)
    humidity = round(random.uniform(0.65, 0.95), 2)
    pressure = round(random.uniform(1.000, 2.000), 3)
    body = '{"temp":%s,"humidity":%s,"pressure":%s}' % (temp, humidity, pressure)

    # Publish to rabbitmq
    channel.basic_publish(exchange='amq.direct', routing_key=routing_key, body=body)
    time.sleep(seconds)

    print(" [Sensor] Sent %s " % (body))

connection.close()
