import "dotenv/config";

import axios from "axios";
import { readFileSync } from "fs";
import { join } from "path";

const EXCHANGE_API_URL =
  process.env.EXCHANGE_API_URL || "http://localhost:8888";

async function main() {
  const cookieSeedsPath = join(__dirname, "..", "data", "cookies.seed.json");
  const buffers = readFileSync(cookieSeedsPath, "utf-8");

  // Parse JSON
  const cookies: any[] = JSON.parse(buffers.toString());

  // Concurrently register balances (BTC, USDT)
  const currencies = ["BTC", "USDT"];
  const promises = cookies.map((cookie) =>
    currencies.map(async (currency) => {
      // Register balance
      await axios.post(
        `${EXCHANGE_API_URL}/api/v1/balances/create`,
        { currency },
        { headers: { cookie } }
      );
      // Deposit balance
      await axios.post(
        `${EXCHANGE_API_URL}/api/v1/balances/deposit`,
        { currency, amount: 100000000 },
        { headers: { cookie } }
      );
    })
  );

  await Promise.all(promises);

  console.log(`Genereated ${cookies.length * 2} balances successfully!`);
}

main().catch((err) => console.log(err));
