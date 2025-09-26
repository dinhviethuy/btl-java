import axios from "axios";
import dotenv from "dotenv";
import fs from "fs";
import path from "path";

// Load env cascade: .env, .env.local, .env.development/.env.production
(() => {
  const cwd = process.cwd();
  const candidates = [
    path.join(cwd, ".env"),
    path.join(cwd, ".env.local"),
    path.join(cwd, ".env.development"),
    path.join(cwd, ".env.production"),
  ].filter((p) => fs.existsSync(p));
  for (const p of candidates) {
    dotenv.config({ path: p, override: false });
  }
})();

const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

const argv = Object.fromEntries(
  process.argv.slice(2).map((arg) => {
    const [k, v] = arg.replace(/^--/, "").split("=");
    return [k, v ?? true];
  })
);

const BACKEND_URL =
  process.env.VITE_BACKEND_URL ||
  process.env.BACKEND_URL ||
  "http://localhost:8080";
const ADMIN_USER = process.env.ADMIN_USER || "admin@example.com";
const ADMIN_PASS = process.env.ADMIN_PASS || "123456";
const COMPANY_ID = process.env.COMPANY_ID || undefined; // optional: fixed company id

const DAYS = Number(argv.days || 30);
const PER_DAY = Number(argv.perDay || 50);
const MIN_PER_DAY = argv.minPerDay ? Number(argv.minPerDay) : undefined;
const MAX_PER_DAY = argv.maxPerDay ? Number(argv.maxPerDay) : undefined;
const DRY_RUN = Boolean(argv.dry || argv["dry-run"] || false);

const SKILLS = [
  "JAVASCRIPT",
  "TYPESCRIPT",
  "REACT.JS",
  "NODE.JS",
  "NEST.JS",
  "JAVA",
  "SPRING BOOT",
  "MONGODB",
  "POSTGRESQL",
  "DOCKER",
];
const LEVELS = ["INTERN", "FRESHER", "JUNIOR", "MIDDLE", "SENIOR"];
const LOCATIONS = ["HANOI", "HOCHIMINH", "DANANG", "OTHER"];

function randomInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

function randomPick(list, count) {
  const arr = [...list];
  const out = [];
  for (let i = 0; i < Math.min(count, arr.length); i += 1) {
    const idx = randomInt(0, arr.length - 1);
    out.push(arr[idx]);
    arr.splice(idx, 1);
  }
  return out;
}

function unwrap(res) {
  // Supports both raw payload and wrapped { data: T }
  if (!res) return undefined;
  if (res.data && typeof res.data === "object" && "data" in res.data)
    return res.data.data;
  return res.data;
}

async function login() {
  const url = `${BACKEND_URL}/api/v1/auth/login`;
  const res = await axios.post(
    url,
    { username: ADMIN_USER, password: ADMIN_PASS },
    { withCredentials: true }
  );
  const payload = unwrap(res);
  const token = payload?.access_token;
  if (!token) throw new Error("Cannot get access_token. Check credentials.");
  return token;
}

async function fetchCompanies(token) {
  const url = `${BACKEND_URL}/api/v1/companies?current=1&pageSize=200`;
  const res = await axios.get(url, {
    headers: { Authorization: `Bearer ${token}` },
  });
  const payload = unwrap(res);
  return payload?.result ?? [];
}

const MIN_QTY = argv.minQty ? Number(argv.minQty) : 1;
const MAX_QTY = argv.maxQty ? Number(argv.maxQty) : 10;

function startOfDay(date) {
  const d = new Date(date);
  d.setHours(0, 0, 0, 0);
  return d;
}

function addDays(date, days) {
  const d = new Date(date);
  d.setDate(d.getDate() + days);
  return d;
}

function buildJobPayload(company, when) {
  const salary = randomInt(8, 60) * 100; // 800 -> 6000
  const quantity = randomInt(MIN_QTY, Math.max(MIN_QTY, MAX_QTY));
  const lvCount = randomInt(1, 3);
  const pickedLevels = randomPick(LEVELS, lvCount);
  const skCount = randomInt(2, 5);
  const pickedSkills = randomPick(SKILLS, skCount);
  const location = LOCATIONS[randomInt(0, LOCATIONS.length - 1)];

  // đảm bảo công khai: startDate <= hôm nay, endDate >= hôm nay
  const startBase = startOfDay(when || new Date());
  const startDate = addDays(startBase, -randomInt(0, 2));
  const endDate = addDays(startBase, randomInt(15, 180));

  return {
    name: `Engineer ${pickedSkills[0]} ${new Date()
      .getTime()
      .toString(36)
      .slice(-4)}`,
    skills: pickedSkills,
    company: company
      ? { _id: company._id, name: company.name, logo: company.logo }
      : undefined,
    location,
    salary,
    quantity,
    levels: pickedLevels,
    description: `Auto-generated job for ${
      company?.name || "Random Company"
    }. Skills: ${pickedSkills.join(", ")}. Levels: ${pickedLevels.join(", ")}`,
    startDate,
    endDate,
    isActive: true,
  };
}

async function createJob(token, payload, createdAtIso) {
  const url = `${BACKEND_URL}/api/v1/jobs`;
  // backend will set createdAt automatically; if backend supports it, include createdAt. Otherwise, seed spread over days by delaying calls.
  const res = await axios.post(url, payload, {
    headers: { Authorization: `Bearer ${token}` },
  });
  const data = unwrap(res) || res?.data;
  // Best effort: if expose update endpoint, patch createdAt (optional, depends on backend policy)
  if (createdAtIso) {
    try {
      const jobId = data?._id || data?.id;
      if (jobId)
        await axios.patch(
          `${BACKEND_URL}/api/v1/jobs/${jobId}`,
          { createdAt: createdAtIso },
          { headers: { Authorization: `Bearer ${token}` } }
        );
    } catch {
      /* ignore if not allowed */
    }
  }
  return data;
}

async function main() {
  const plannedAvgPerDay =
    MIN_PER_DAY && MAX_PER_DAY
      ? Math.round((MIN_PER_DAY + MAX_PER_DAY) / 2)
      : PER_DAY;
  console.log(
    `[seed-jobs] Start. days=${DAYS}, perDay=${PER_DAY}${
      MIN_PER_DAY ? `, minPerDay=${MIN_PER_DAY}` : ""
    }${
      MAX_PER_DAY ? `, maxPerDay=${MAX_PER_DAY}` : ""
    }, qty=[${MIN_QTY}-${Math.max(MIN_QTY, MAX_QTY)}], backend=${BACKEND_URL}`
  );
  const token = process.env.ACCESS_TOKEN || (await login());
  let companies = [];
  try {
    companies = await fetchCompanies(token);
  } catch {
    /* ignore */
  }
  if (COMPANY_ID && !companies.find((c) => c._id === COMPANY_ID))
    companies.push({ _id: COMPANY_ID, name: "Company From ENV" });

  const totalPlanned = DAYS * plannedAvgPerDay;
  let done = 0;
  for (let d = DAYS - 1; d >= 0; d -= 1) {
    const day = new Date();
    day.setHours(12, 0, 0, 0);
    day.setDate(day.getDate() - d);

    const jobsToday =
      MIN_PER_DAY !== undefined && MAX_PER_DAY !== undefined
        ? randomInt(MIN_PER_DAY, MAX_PER_DAY)
        : PER_DAY;
    for (let i = 0; i < jobsToday; i += 1) {
      // Rải rác trong ngày: giờ 8-22h, phút/giây/mili random
      const when = new Date(day);
      const hour = randomInt(8, 22);
      const minute = randomInt(0, 59);
      const second = randomInt(0, 59);
      const ms = randomInt(0, 999);
      when.setHours(hour, minute, second, ms);
      const createdAtIso = when.toISOString();
      const company = companies.length
        ? companies[randomInt(0, companies.length - 1)]
        : undefined;
      const payload = buildJobPayload(company, when);
      if (DRY_RUN) {
        done += 1;
        if (done % 50 === 0) console.log(`[dry] ${done}/${totalPlanned}`);
        continue;
      }
      try {
        await createJob(token, payload, createdAtIso);
        done += 1;
        if (done % 20 === 0) console.log(`[seed] ${done}/${totalPlanned}`);
      } catch (e) {
        console.error("Create job failed:", e?.response?.data || e?.message);
      }
      await sleep(50); // small delay to avoid overloading server
    }
  }
  console.log(`[seed-jobs] Done. created ~${DRY_RUN ? 0 : done} jobs`);
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
