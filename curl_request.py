import requests

url = "https://eoo65j3tdpgnqoc.m.pipedream.net"

proxies = {
    "http": "http://localhost:8899",  
    "https": "http://localhost:8899"  
}

response = requests.get(url, proxies=proxies)

print("Response Status Code:", response.status_code)
print("Response Content:", response.text)
