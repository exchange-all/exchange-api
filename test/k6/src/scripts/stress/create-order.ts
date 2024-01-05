import { check } from "k6";
import { cookieJar, post } from "k6/http";
import { Options } from "k6/options";

const cookieSeeds = JSON.parse(open("../../data/cookies.seed.json"));

const types = ["CREATE_ASK_LIMIT_ORDER", "CREATE_BID_LIMIT_ORDER"];

const getOrderPayload = {
  bid: getBidData,
  ask: getAskData,
};

function getBidData() {
  return {
    baseCurrency: "BTC",
    quoteCurrency: "USDT",
    price: Math.floor(getRandomPrice(35000, 47000)),
    amount: getRandomPrice(0.0001, 5.5),
  };
}

function getAskData() {
  return {
    baseCurrency: "BTC",
    quoteCurrency: "USDT",
    price: Math.floor(getRandomPrice(35000, 47000)),
    amount: getRandomPrice(0.0001, 5.5),
  };
}

function getRandomPrice(min: number, max: number) {
  return Math.random() * (max - min) + min;
}

export const options: Options = {
  stages: [
    { duration: "1m", target: 100 }, // traffic ramp-up from 1 to a higher 100 users over 1 minutes.
    { duration: "3m", target: 500 }, // stay at higher 500 users for 3 minutes
    { duration: "1m", target: 0 }, // ramp-down to 0 users
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
  const url = "http://localhost:8000/exchange/api/v1/order-book/create-order";
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
