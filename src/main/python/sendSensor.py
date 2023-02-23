import time
import pika
import random

connection = pika.BlockingConnection(pika.ConnectionParameters('localhost', port='5672'))
channel = connection.channel()
print(connection)

channel.queue_declare(queue='sensor', durable=True)

while(True):
    # Generate random data
    temp = round(random.uniform(18.00, 22.99), 2)
    humidity = round(random.uniform(0.65, 0.95), 2)
    pressure = round(random.uniform(1.000, 2.000), 3)
    body = '{"temp":%s,"humidity":%s,"pressure":%s}' % (temp, humidity, pressure)

    # Publish to rabbitmq
    channel.basic_publish(exchange='amq.direct', routing_key='sensor', body=body)
    time.sleep(1)

    print(" [Sensor] Sent %s " % (body))

connection.close()
