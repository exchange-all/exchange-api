import "dotenv/config";

import axios from "axios";
import { writeFileSync } from "fs";
import { join } from "path";
import { performance } from "perf_hooks";

const EXCHANGE_API_URL =
  process.env.EXCHANGE_API_URL || "http://localhost:8888";

async function main() {
  const start = performance.now();
  const generatedUsers = [];

  for (let i = 0; i < +process.env.USER_SEED_TARGET; i++) {
    const user = {
      email: `testuser${i}@gmail.com`,
      password: "1234abcd",
    };
    try {
      const response = await axios.post(
        `${EXCHANGE_API_URL}/auth/api/v1/auth/register-with-email`,
        user
      );
      if (response.status === 200) {
        generatedUsers.push(user);
      }
    } catch (err) {}
  }

  // Write to file
  const filePath = join(__dirname, "..", "data", "users.seed.json");
  writeFileSync(filePath, JSON.stringify(generatedUsers, null, 2));

  console.log(
    `Genereated ${generatedUsers.length} users successfully after ${
      performance.now() - start
    } milliseconds!`
  );
}

main().catch((err) => console.log(err));
