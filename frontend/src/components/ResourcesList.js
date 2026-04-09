import React from 'react';
import '../styles/ResourcesList.css';

export function ResourcesList({ resources }) {
  const groupedResources = resources.reduce((acc, resource) => {
    const type = resource.type || 'unknown';
    if (!acc[type]) {
      acc[type] = [];
    }
    acc[type].push(resource);
    return acc;
  }, {});

  const getResourceIcon = (type) => {
    const icons = {
      'ec2': '🖥️',
      'rds': '🗄️',
      's3': '📦',
      'lambda': '⚡',
      'load_balancer': '⚖️',
    };
    return icons[type] || '☁️';
  };

  const getResourceLabel = (type) => {
    const labels = {
      'ec2': 'EC2 Instance',
      'rds': 'RDS Database',
      's3': 'S3 Bucket',
      'lambda': 'Lambda Function',
      'load_balancer': 'Load Balancer',
    };
    return labels[type] || type;
  };

  return (
    <div className="resources-list">
      <h2>Detected AWS Resources</h2>

      <div className="resources-container">
        {Object.entries(groupedResources).map(([type, resourceList]) => (
          <div key={type} className="resource-group">
            <h3>
              <span className="icon">{getResourceIcon(type)}</span>
              {getResourceLabel(type)}
              <span className="count">{resourceList.length}</span>
            </h3>

            <div className="resource-items">
              {resourceList.map((resource, index) => (
                <div key={index} className="resource-item">
                  <div className="resource-details">
                    {resource.instance_type && (
                      <div className="detail">
                        <span className="label">Instance Type:</span>
                        <span className="value">{resource.instance_type}</span>
                      </div>
                    )}
                    {resource.count && (
                      <div className="detail">
                        <span className="label">Count:</span>
                        <span className="value">{resource.count}</span>
                      </div>
                    )}
                    {resource.size && (
                      <div className="detail">
                        <span className="label">Size:</span>
                        <span className="value">{resource.size}</span>
                      </div>
                    )}
                    {resource.tier && (
                      <div className="detail">
                        <span className="label">Tier:</span>
                        <span className="value">{resource.tier}</span>
                      </div>
                    )}
                    {resource.region && (
                      <div className="detail">
                        <span className="label">Region:</span>
                        <span className="value">{resource.region || 'us-east-1'}</span>
                      </div>
                    )}
                  </div>
                  {resource.source_file && (
                    <div className="source-file">
                      📄 {resource.source_file.split('/').pop()}
                    </div>
                  )}
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

