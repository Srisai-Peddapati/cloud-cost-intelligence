import React, { useState } from 'react';
import { ChevronDown } from 'lucide-react';
import '../styles/OptimizationsList.css';

export function OptimizationsList({ optimizations }) {
  const [expandedId, setExpandedId] = useState(null);

  const getPriorityColor = (priority) => {
    if (priority >= 4) return '#ef4444';
    if (priority >= 3) return '#f59e0b';
    return '#10b981';
  };

  const getComplexityColor = (complexity) => {
    switch (complexity) {
      case 'low': return '#10b981';
      case 'medium': return '#f59e0b';
      case 'high': return '#ef4444';
      default: return '#6b7280';
    }
  };

  const toggleExpand = (id) => {
    setExpandedId(expandedId === id ? null : id);
  };

  return (
    <div className="optimizations-list">
      <h2>Recommended Optimizations</h2>
      <p className="subtitle">
        Implement these recommendations to reduce your AWS costs
      </p>

      <div className="optimizations-container">
        {optimizations.length === 0 ? (
          <p className="no-optimizations">No optimizations available for your infrastructure.</p>
        ) : (
          optimizations.map((opt, index) => (
            <div key={index} className="optimization-card">
              <div
                className="optimization-header"
                onClick={() => toggleExpand(index)}
              >
                <div className="header-left">
                  <h3>{opt.suggestion}</h3>
                  <div className="badges">
                    <span
                      className="badge priority"
                      style={{ backgroundColor: getPriorityColor(opt.priority) }}
                    >
                      Priority {opt.priority}
                    </span>
                    <span
                      className="badge complexity"
                      style={{ backgroundColor: getComplexityColor(opt.complexity) }}
                    >
                      {opt.complexity}
                    </span>
                    <span className="badge category">
                      {opt.category}
                    </span>
                  </div>
                </div>
                <div className="header-right">
                  <div className="savings">
                    <div className="savings-item">
                      <span className="label">Monthly Savings:</span>
                      <span className="value">${opt.monthly_savings?.toFixed(2) || '0.00'}</span>
                    </div>
                  </div>
                  <ChevronDown
                    size={20}
                    className={`chevron ${expandedId === index ? 'expanded' : ''}`}
                  />
                </div>
              </div>

              {expandedId === index && (
                <div className="optimization-details">
                  <div className="detail-section">
                    <h4>Description</h4>
                    <p>{opt.description}</p>
                  </div>

                  <div className="detail-section">
                    <h4>Potential Savings</h4>
                    <div className="savings-detail">
                      <div className="saving-item">
                        <span className="label">Monthly:</span>
                        <span className="value">${opt.monthly_savings?.toFixed(2) || '0.00'}</span>
                      </div>
                      <div className="saving-item">
                        <span className="label">Yearly:</span>
                        <span className="value">${opt.yearly_savings?.toFixed(2) || '0.00'}</span>
                      </div>
                      <div className="saving-item">
                        <span className="label">Reduction:</span>
                        <span className="value">{opt.savings_percentage || '0%'}</span>
                      </div>
                    </div>
                  </div>

                  <div className="detail-section">
                    <h4>Trade-offs</h4>
                    <div className="tradeoff-warning">
                      <p>{opt.tradeoff}</p>
                    </div>
                  </div>
                </div>
              )}
            </div>
          ))
        )}
      </div>
    </div>
  );
}

