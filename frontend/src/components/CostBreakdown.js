import React from 'react';
import { PieChart, Pie, Cell, Legend, Tooltip, ResponsiveContainer } from 'recharts';
import '../styles/CostBreakdown.css';

const COLORS = ['#3b82f6', '#ef4444', '#10b981', '#f59e0b'];

export function CostBreakdown({ breakdown }) {
  const data = [
    { name: 'Compute', value: breakdown.compute_cost },
    { name: 'Database', value: breakdown.database_cost },
    { name: 'Storage', value: breakdown.storage_cost },
    { name: 'Load Balancer', value: breakdown.load_balancer_cost }
  ].filter(item => item.value > 0);

  return (
    <div className="cost-breakdown">
      <h2>Cost Breakdown</h2>

      <div className="breakdown-container">
        <div className="breakdown-chart">
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie
                data={data}
                cx="50%"
                cy="50%"
                labelLine={false}
                label={({ name, value }) => `${name}: $${value.toFixed(2)}`}
                outerRadius={80}
                fill="#8884d8"
                dataKey="value"
              >
                {data.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                ))}
              </Pie>
              <Tooltip formatter={(value) => `$${value.toFixed(2)}`} />
            </PieChart>
          </ResponsiveContainer>
        </div>

        <div className="breakdown-details">
          <div className="breakdown-item">
            <span className="label">Compute Cost:</span>
            <span className="value">${breakdown.compute_cost.toFixed(2)}</span>
          </div>
          <div className="breakdown-item">
            <span className="label">Database Cost:</span>
            <span className="value">${breakdown.database_cost.toFixed(2)}</span>
          </div>
          <div className="breakdown-item">
            <span className="label">Storage Cost:</span>
            <span className="value">${breakdown.storage_cost.toFixed(2)}</span>
          </div>
          <div className="breakdown-item">
            <span className="label">Load Balancer Cost:</span>
            <span className="value">${breakdown.load_balancer_cost.toFixed(2)}</span>
          </div>
          <div className="breakdown-item total">
            <span className="label">Total Monthly Cost:</span>
            <span className="value">${breakdown.total_cost.toFixed(2)}</span>
          </div>
        </div>
      </div>
    </div>
  );
}

