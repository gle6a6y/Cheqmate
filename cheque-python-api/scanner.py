import requests
import json

url = "https://proverkacheka.com/api/v1/check/get"

def get_cheque(qr: str) -> dict:
    data = {
    "token": "38902.zfRxwRAtOWSQDy22v",
    "qrraw": qr,
    }
    ticket = requests.post(url, data=data)
    return ticket.json()