import "dotenv/config";

import axios from "axios";
import { readFileSync, writeFileSync } from "fs";
import { join } from "path";
import { performance } from "perf_hooks";

const EXCHANGE_API_URL =
  process.env.EXCHANGE_API_URL || "http://localhost:8888";

async function main() {
  const start = performance.now();
  // Read user seed file
  const userSeedsPath = join(__dirname, "..", "data", "users.seed.json");
  const buffers = readFileSync(userSeedsPath, "utf-8");

  // Parse JSON
  const users: any[] = JSON.parse(buffers.toString());

  const promises = users.map((user) =>
    axios.post(`${EXCHANGE_API_URL}/api/v1/auth/login-with-email`, user)
  );

  const responses = await Promise.all(promises);

  const cookieSessions = [];

  // Extract session from cookie
  responses.forEach((response) => {
    const cookie = response.headers["set-cookie"][0];
    const session = cookie.split(";")[0];
    cookieSessions.push(session);
  });

  // Write to file
  const cookieSeedsPath = join(__dirname, "..", "data", "cookies.seed.json");
  writeFileSync(cookieSeedsPath, JSON.stringify(cookieSessions, null, 2));

  console.log(
    `Genereated ${cookieSessions.length} cookies successfully after ${
      performance.now() - start
    } milliseconds!`
  );
}

main().catch((err) => console.log(err));
