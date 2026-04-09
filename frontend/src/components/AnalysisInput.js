import React from 'react';
import '../styles/App.css';

export function AnalysisInput({ onAnalyze, loading }) {
  const [url, setUrl] = React.useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    if (url.trim()) {
      onAnalyze(url);
    }
  };

  return (
    <div className="input-section">
      <div className="input-container">
        <h2>Enter GitLab Repository URL</h2>
        <form onSubmit={handleSubmit}>
          <input
            type="text"
            value={url}
            onChange={(e) => setUrl(e.target.value)}
            placeholder="https://gitlab.com/user/repo or https://github.com/user/repo"
            disabled={loading}
            className="repo-input"
          />
          <button type="submit" disabled={loading || !url.trim()} className="analyze-button">
            {loading ? 'Analyzing...' : 'Analyze'}
          </button>
        </form>
      </div>
    </div>
  );
}

