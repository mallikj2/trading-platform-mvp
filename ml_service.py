import json
from kafka import KafkaConsumer, KafkaProducer
import time
import random
from datetime import datetime

# Kafka Configuration
BOOTSTRAP_SERVERS = 'localhost:9092'
STOCK_DATA_TOPIC = 'stock-data-topic'
ML_PREDICTIONS_TOPIC = 'ml-predictions-topic'

# Initialize Kafka Consumer
consumer = KafkaConsumer(
    STOCK_DATA_TOPIC,
    bootstrap_servers=BOOTSTRAP_SERVERS,
    value_deserializer=lambda m: json.loads(m.decode('utf-8')),
    group_id='ml-service-group'
)

# Initialize Kafka Producer
producer = KafkaProducer(
    bootstrap_servers=BOOTSTRAP_SERVERS,
    value_serializer=lambda m: json.dumps(m).encode('utf-8')
)

print(f"Listening for messages on topic: {STOCK_DATA_TOPIC}")

for message in consumer:
    stock_data = message.value
    symbol = stock_data['symbol']
    timestamp = stock_data['timestamp']
    close_price = stock_data['close']

    print(f"Received stock data for {symbol} at {timestamp} (Close: {close_price})")

    # --- Simulate ML Model Prediction ---
    # In a real scenario, you would feed stock_data into your trained ML model
    # and get a prediction (e.g., BUY, SELL, HOLD) and a confidence score.

    prediction_type = random.choice(["BUY", "SELL", "HOLD"])
    confidence = round(random.uniform(0.5, 0.99), 2) # Simulate confidence

    # Example: Simple rule-based prediction for demonstration
    if close_price > 172.0:
        prediction_type = "SELL"
        confidence = round(random.uniform(0.8, 0.99), 2)
    elif close_price < 170.0:
        prediction_type = "BUY"
        confidence = round(random.uniform(0.8, 0.99), 2)
    else:
        prediction_type = "HOLD"
        confidence = round(random.uniform(0.5, 0.7), 2)

    ml_prediction = {
        "symbol": symbol,
        "timestamp": timestamp, # Use the original timestamp
        "prediction": prediction_type,
        "confidence": confidence
    }

    # Publish ML prediction to Kafka
    producer.send(ML_PREDICTIONS_TOPIC, key=symbol.encode('utf-8'), value=ml_prediction)
    print(f"Published ML prediction for {symbol}: {prediction_type} (Confidence: {confidence})")

    time.sleep(0.1) # Simulate some processing time
