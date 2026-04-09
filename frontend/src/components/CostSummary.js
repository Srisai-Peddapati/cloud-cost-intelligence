import React from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import '../styles/CostSummary.css';

export function CostSummary({ costEstimate }) {
  const data = [
    {
      name: 'Low Traffic',
      monthly: costEstimate.low_traffic.monthly_cost,
      yearly: costEstimate.low_traffic.yearly_cost
    },
    {
      name: 'Medium Traffic',
      monthly: costEstimate.medium_traffic.monthly_cost,
      yearly: costEstimate.medium_traffic.yearly_cost
    },
    {
      name: 'High Traffic',
      monthly: costEstimate.high_traffic.monthly_cost,
      yearly: costEstimate.high_traffic.yearly_cost
    }
  ];

  return (
    <div className="cost-summary">
      <h2>Cost Estimates</h2>

      <div className="summary-cards">
        <div className="summary-card">
          <h3>Low Traffic (100 req/min)</h3>
          <div className="cost-amounts">
            <div className="cost-item">
              <span className="label">Monthly:</span>
              <span className="value">${costEstimate.low_traffic.monthly_cost.toFixed(2)}</span>
            </div>
            <div className="cost-item">
              <span className="label">Yearly:</span>
              <span className="value">${costEstimate.low_traffic.yearly_cost.toFixed(2)}</span>
            </div>
          </div>
        </div>

        <div className="summary-card">
          <h3>Medium Traffic (1000 req/min)</h3>
          <div className="cost-amounts">
            <div className="cost-item">
              <span className="label">Monthly:</span>
              <span className="value">${costEstimate.medium_traffic.monthly_cost.toFixed(2)}</span>
            </div>
            <div className="cost-item">
              <span className="label">Yearly:</span>
              <span className="value">${costEstimate.medium_traffic.yearly_cost.toFixed(2)}</span>
            </div>
          </div>
        </div>

        <div className="summary-card">
          <h3>High Traffic (10000 req/min)</h3>
          <div className="cost-amounts">
            <div className="cost-item">
              <span className="label">Monthly:</span>
              <span className="value">${costEstimate.high_traffic.monthly_cost.toFixed(2)}</span>
            </div>
            <div className="cost-item">
              <span className="label">Yearly:</span>
              <span className="value">${costEstimate.high_traffic.yearly_cost.toFixed(2)}</span>
            </div>
          </div>
        </div>
      </div>

      <div className="chart-container">
        <ResponsiveContainer width="100%" height={300}>
          <BarChart data={data}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="name" />
            <YAxis />
            <Tooltip formatter={(value) => `$${value.toFixed(2)}`} />
            <Legend />
            <Bar dataKey="monthly" fill="#3b82f6" name="Monthly Cost" />
            <Bar dataKey="yearly" fill="#ef4444" name="Yearly Cost" />
          </BarChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}

