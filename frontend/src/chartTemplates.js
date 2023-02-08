export const latencyChart = {
  labels: [],
  datasets: [
    {
      label: "Latency (ms)",
      fill: false,
      tension: 0.1,
      borderColor: "#14b8a6",
      data: [],
    },
  ],
};

export const cpuChart = {
  labels: [],
  datasets: [
    {
      label: "CPU Usage (%)",
      fill: false,
      tension: 0.1,
      borderColor: "#06b6d4",
      data: [],
    },
    {
      label: "IOWait",
      fill: false,
      tension: 0.1, 
      borderColor: "#3b82f6",
      data: [],
    },
  ],
};

export const ramChart = {
  labels: [],
  datasets: [
    {
      label: "RAM Usage (%)",
      fill: false,
      tension: 0.1,
      borderColor: "#22c55e",
      data: [],
    },
    {
      label: "SWAP Usage (%)",
      fill: false,
      tension: 0.1, 
      borderColor: "#84cc16",
      data: [],
    },
  ],
};

export const netChart = {
  labels: [],
  datasets: [
    {
      label: "Received (MB/s)",
      fill: false,
      tension: 0.1,
      borderColor: "#ef4444",
      data: [],
    },
    {
      label: "Transmitted (MB/s)",
      fill: false,
      tension: 0.1, 
      borderColor: "#f97316",
      data: [],
    },
  ],
};

export const diskChart = {
  labels: [],
  datasets: [
    {
      label: "Disk usage (summarized %)",
      fill: false,
      tension: 0.1, 
      borderColor: "#8b5cf6",
      data: [],
    },
  ],
};


