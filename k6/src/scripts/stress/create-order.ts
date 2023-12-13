import { check } from "k6";
import { cookieJar, post } from "k6/http";
import { Options } from "k6/options";

const cookieSeeds = JSON.parse(open("../../data/cookies.seed.json"));

const CREATE_BID_API_URL =
  "http://localhost:8888/api/v1/order-book/create-bid-limit-order";
const CREATE_ASK_API_URL =
  "http://localhost:8888/api/v1/order-book/create-ask-limit-order";

const types = ["bid", "ask"];

const typeUrl = {
  bid: CREATE_BID_API_URL,
  ask: CREATE_ASK_API_URL,
};

const getOrderPayload = {
  bid: getBidData,
  ask: getAskData,
};

function getBidData() {
  return {
    baseCurrency: "BTC",
    quoteCurrency: "USDT",
    price: 1,
    amount: 1,
  };
}

function getAskData() {
  return {
    baseCurrency: "BTC",
    quoteCurrency: "USDT",
    price: 1,
    amount: 1,
  };
}

export const options: Options = {
  stages: [
    { duration: "1m", target: 300 }, // traffic ramp-up from 1 to a higher 300 users over 1 minutes.
    { duration: "5m", target: 1000 }, // stay at higher 1000 users for 5 minutes
    { duration: "3m", target: 0 }, // ramp-down to 0 users
  ],
  thresholds: {
    // Required to mark the test as passed/failed
    checks: ["rate > 0.9"], // Passing failure rate < 10%
  },
};

export default function () {
  const sessionCookie =
    cookieSeeds[Math.floor(Math.random() * cookieSeeds.length)];

  const type = types[Math.floor(Math.random() * types.length)];
  const url = typeUrl[type];
  const payload = getOrderPayload[type]();

  const SESSION_KEY = sessionCookie.split("=")[0];
  const SESSION_VALUE = sessionCookie.split("=")[1];

  const jar = cookieJar();
  jar.set(url, SESSION_KEY, SESSION_VALUE);

  const res = post(url, JSON.stringify(payload), {
    headers: {
      "Content-Type": "application/json",
    },
  });

  check(res, {
    "status is 200": (r) => r.status === 200,
  });
}
