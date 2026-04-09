import React, { useState } from 'react';
import axios from 'axios';
import { AnalysisInput } from './components/AnalysisInput';
import { CostSummary } from './components/CostSummary';
import { CostBreakdown } from './components/CostBreakdown';
import { OptimizationsList } from './components/OptimizationsList';
import { ResourcesList } from './components/ResourcesList';
import './App.css';

function App() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [analysis, setAnalysis] = useState(null);

  const handleAnalyze = async (repositoryUrl) => {
    setLoading(true);
    setError(null);

    try {
      const response = await axios.post('http://localhost:8080/api/analyze', {
        repositoryUrl: repositoryUrl
      });
      setAnalysis(response.data);
    } catch (err) {
      setError(
        err.response?.data?.error ||
        'Failed to analyze repository. Please check the URL and try again.'
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="App">
      <header className="header">
        <div className="header-content">
          <h1>AWS Cost Intelligence Analyzer</h1>
          <p>Analyze IaC repositories and estimate AWS infrastructure costs</p>
        </div>
      </header>

      <main className="main-content">
        <AnalysisInput onAnalyze={handleAnalyze} loading={loading} />

        {error && (
          <div className="error-message">
            <p>{error}</p>
          </div>
        )}

        {analysis && (
          <div className="analysis-results">
            <div className="results-grid">
              <CostSummary costEstimate={analysis.costEstimate} />
              <ResourcesList resources={analysis.resources} />
            </div>

            <CostBreakdown breakdown={analysis.costEstimate.breakdown} />

            <OptimizationsList optimizations={analysis.optimizations} />
          </div>
        )}

        {loading && (
          <div className="loading">
            <div className="spinner"></div>
            <p>Analyzing repository...</p>
          </div>
        )}
      </main>

      <footer className="footer">
        <p>&copy; 2024 AWS Cost Intelligence Analyzer. Made for cost-conscious developers.</p>
      </footer>
    </div>
  );
}

export default App;

