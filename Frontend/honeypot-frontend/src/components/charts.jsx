import {
  ResponsiveContainer,
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  BarChart,
  Bar,
  Cell,
  PieChart,
  Pie,
} from "recharts";
import { sevColor, stageColor, eventLabel } from "../utils/format";

const AXIS = { stroke: "#6b7c9c", fontSize: 11, tickLine: false, axisLine: false };
const GRID = "rgba(255,255,255,0.05)";
const PALETTE = [
  "#22d3ee", "#6366f1", "#38bdf8", "#818cf8", "#2dd4bf",
  "#60a5fa", "#a78bfa", "#34d399", "#f472b6", "#fbbf24",
];

/* --------------------------- bucket helpers --------------------------- */

function parseBucket(b) {
  if (!b) return null;
  let s = String(b).trim().replace(" ", "T");
  if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(s)) s += ":00";
  const d = new Date(s.endsWith("Z") ? s : `${s}Z`);
  return Number.isNaN(d.getTime()) ? null : d;
}

export function bucketShort(b) {
  const d = parseBucket(b);
  if (!d) return b;
  return d.toLocaleString(undefined, { month: "short", day: "2-digit", hour: "2-digit", minute: "2-digit", hour12: false });
}

function bucketLong(b) {
  const d = parseBucket(b);
  if (!d) return b;
  return d.toLocaleString(undefined, { year: "numeric", month: "short", day: "2-digit", hour: "2-digit", minute: "2-digit", hour12: false });
}

/* ------------------------------- tooltip ------------------------------ */

function Tip({ active, payload, label, labelFormatter }) {
  if (!active || !payload?.length) return null;
  return (
    <div className="chart-tip">
      {label != null && <div className="chart-tip-title">{labelFormatter ? labelFormatter(label) : label}</div>}
      {payload.map((p, i) => (
        <div className="chart-tip-row" key={i}>
          <span className="swatch" style={{ background: p.color || p.fill || p.payload?.fill }} />
          <span className="k">{p.name}</span>
          <strong>{p.value}</strong>
        </div>
      ))}
    </div>
  );
}

/* ------------------------------ timeline ------------------------------ */

export function TimelineChart({ data = [] }) {
  return (
    <ResponsiveContainer width="100%" height={264}>
      <AreaChart data={data} margin={{ top: 10, right: 12, left: -16, bottom: 0 }}>
        <defs>
          <linearGradient id="gCount" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="#22d3ee" stopOpacity={0.45} />
            <stop offset="100%" stopColor="#22d3ee" stopOpacity={0.02} />
          </linearGradient>
          <linearGradient id="gCrit" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="#f43f5e" stopOpacity={0.45} />
            <stop offset="100%" stopColor="#f43f5e" stopOpacity={0.02} />
          </linearGradient>
        </defs>
        <CartesianGrid stroke={GRID} vertical={false} />
        <XAxis dataKey="bucket" tickFormatter={bucketShort} minTickGap={28} {...AXIS} />
        <YAxis allowDecimals={false} width={42} {...AXIS} />
        <Tooltip content={<Tip labelFormatter={bucketLong} />} cursor={{ stroke: "#2c3f63" }} />
        <Area type="monotone" dataKey="count" name="Events" stroke="#22d3ee" strokeWidth={2} fill="url(#gCount)" />
        <Area type="monotone" dataKey="critical" name="Critical" stroke="#f43f5e" strokeWidth={2} fill="url(#gCrit)" />
      </AreaChart>
    </ResponsiveContainer>
  );
}

/* ---------------------------- event types ----------------------------- */

export function EventTypesChart({ data = [] }) {
  const rows = data.map((d) => ({ ...d, name: eventLabel(d.name) }));
  const height = Math.max(220, rows.length * 32 + 24);
  return (
    <ResponsiveContainer width="100%" height={height}>
      <BarChart data={rows} layout="vertical" margin={{ top: 4, right: 18, left: 6, bottom: 4 }}>
        <CartesianGrid stroke={GRID} horizontal={false} />
        <XAxis type="number" allowDecimals={false} {...AXIS} />
        <YAxis type="category" dataKey="name" width={132} {...AXIS} tick={{ fontSize: 11, fill: "#a9b8d6" }} />
        <Tooltip content={<Tip />} cursor={{ fill: "rgba(255,255,255,0.03)" }} />
        <Bar dataKey="count" name="Events" radius={[0, 6, 6, 0]} barSize={15}>
          {rows.map((_, i) => (
            <Cell key={i} fill={PALETTE[i % PALETTE.length]} />
          ))}
        </Bar>
      </BarChart>
    </ResponsiveContainer>
  );
}

/* --------------------------- severity donut --------------------------- */

export function SeverityDonut({ data = [] }) {
  const total = data.reduce((s, d) => s + (d.count || 0), 0);
  const rows = data.filter((d) => d.count > 0);
  return (
    <div className="donut-wrap">
      <ResponsiveContainer width="100%" height={224}>
        <PieChart>
          <Pie data={rows} dataKey="count" nameKey="label" innerRadius={60} outerRadius={88} paddingAngle={3} stroke="none">
            {rows.map((d, i) => (
              <Cell key={i} fill={sevColor(d.name)} />
            ))}
          </Pie>
          <Tooltip content={<Tip />} />
        </PieChart>
      </ResponsiveContainer>
      <div className="donut-center">
        <strong>{total}</strong>
        <span>events</span>
      </div>
    </div>
  );
}

/* --------------------------- stage funnel ----------------------------- */

export function StageFunnel({ data = [] }) {
  const max = Math.max(1, ...data.map((d) => d.count || 0));
  return (
    <div className="funnel">
      {data.map((d) => (
        <div className="funnel-row" key={d.name}>
          <div className="funnel-label">{d.label}</div>
          <div className="funnel-track">
            <div
              className="funnel-fill"
              style={{ width: `${((d.count || 0) / max) * 100}%`, background: stageColor(d.name) }}
            />
          </div>
          <div className="funnel-count">{d.count || 0}</div>
        </div>
      ))}
    </div>
  );
}
