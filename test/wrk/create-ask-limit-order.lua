-- TODO: login first, then replace with your session cookie
-- $ wrk -t32 -c600 -d60s -s create-ask-limit-order.lua --latency http://localhost:8888/api/v1/order-book/create-ask-limit-order

wrk.method = "POST"
wrk.body = '{"price": 1, "amount": 1, "baseCurrency": "BTC", "quoteCurrency": "USDT"}'
wrk.headers["Content-Type"] = "application/json"
wrk.headers["Cookie"] = "SESSION=12704b4d-6af7-4582-a138-af9dd7936503" -- replace with your session cookie
