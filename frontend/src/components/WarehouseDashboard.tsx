// src/components/WarehouseDashboard.tsx

import React, { useEffect, useState } from "react";
import { WarehouseDashboard } from "../types/dashboard";

const WarehouseDashboardComponent: React.FC = () => {
  const [data, setData] = useState<WarehouseDashboard[]>([]);
  const [selected, setSelected] = useState<WarehouseDashboard | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

 


  useEffect(() => {
    const fetchDashboard = async () => {
      try {
        setLoading(true);
        setError(null);

        const API = "http://localhost:8080";

        const res = await fetch(`${API}/api/dashboard`);

        if (!res.ok) {
          throw new Error(`HTTP error ${res.status}`);
        }

        const json = (await res.json()) as WarehouseDashboard[];
        setData(json);

        if (json.length > 0) {
          setSelected(json[0]);
        }

      } catch (err) {
        console.error(err);
        setError("Failed to load dashboard data.");
      } finally {
        setLoading(false);
      }
    };
    

    fetchDashboard();
  }, []);

  

  if (loading) return <div>Loading dashboard...</div>;
  if (error) return <div style={{ color: "red" }}>{error}</div>;
  if (data.length === 0) return <div>No dashboard data available.</div>;

  return (
    <div
      style={{
        display: "grid",
        gridTemplateColumns: "2fr 1.5fr",
        gap: "2rem",
        marginTop: "2rem",
      }}
    >
      {/* Left list */}
      <div>
        <h2>Warehouse Usage</h2>
        <div style={{ display: "flex", flexDirection: "column", gap: "0.75rem" }}>
          {data.map((w) => (
            <button
              key={w.warehouseName}
              onClick={() => setSelected(w)}
              style={{
                textAlign: "left",
                padding: "0.75rem",
                borderRadius: "8px",
                border:
                  selected?.warehouseName === w.warehouseName
                    ? "2px solid #0077ff"
                    : "1px solid #ccc",
                backgroundColor: "#fafafa",
                cursor: "pointer",
              }}
            >
              <div style={{ display: "flex", justifyContent: "space-between" }}>
                <strong>{w.warehouseName}</strong>
                <span>{w.utilisationPercent.toFixed(1)}%</span>
              </div>
              <div
                style={{
                  marginTop: "0.25rem",
                  backgroundColor: "#e0e0e0",
                  height: "10px",
                  borderRadius: "999px",
                  overflow: "hidden",
                }}
              >
                <div
                  style={{
                    height: "100%",
                    width: `${Math.min(w.utilisationPercent, 100)}%`,
                    backgroundColor:
                      w.utilisationPercent >= 80 ? "#d9534f" : "#4caf50",
                    transition: "width 0.3s ease",
                  }}
                />
              </div>
            </button>
          ))}
        </div>
      </div>

      {/* Right detail view */}
      <div>
        {selected && (
          <>
            <h2>Details: {selected.warehouseName}</h2>
            <p>
              Total capacity: <strong>{selected.totalCapacity}</strong>
            </p>
            <p>
              Used: <strong>{selected.usedCapacity}</strong>
            </p>
            <p>
              Remaining: <strong>{selected.remainingCapacity}</strong>
            </p>

            <h3 style={{ marginTop: "1.5rem" }}>Temperature</h3>

            {renderTempBar(
              "Ambient",
              selected.temperatureDashboard.ambient,
              selected.usedCapacity
            )}
            {renderTempBar(
              "Cold",
              selected.temperatureDashboard.cold,
              selected.usedCapacity
            )}
            {renderTempBar(
              "Freeze",
              selected.temperatureDashboard.freeze,
              selected.usedCapacity
            )}
          </>
        )}
      </div>
    </div>
  );
};

function renderTempBar(label: string, amount: number, totalUsed: number) {
  const pct = totalUsed > 0 ? (amount * 100) / totalUsed : 0;

  return (
    <div style={{ marginBottom: "1rem" }}>
      <div style={{ display: "flex", justifyContent: "space-between" }}>
        <span>{label}</span>
        <span>
          {amount} pallets ({pct.toFixed(1)}%)
        </span>
      </div>
      <div
        style={{
          marginTop: "0.25rem",
          backgroundColor: "#e0e0e0",
          height: "8px",
          borderRadius: "999px",
          overflow: "hidden",
        }}
      >
        <div
          style={{
            height: "100%",
            width: `${Math.min(pct, 100)}%`,
            backgroundColor: "#2196f3",
            transition: "width 0.3s ease",
          }}
        />
      </div>
    </div>
  );
}

export default WarehouseDashboardComponent;
