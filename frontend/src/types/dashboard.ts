

export type TemperatureDashboard = {
  ambient: number;
  cold: number;
  freeze: number;
};

export type WarehouseDashboard = {
  warehouseName: string;
  totalCapacity: number;
  usedCapacity: number;
  remainingCapacity: number;
  utilisationPercent: number;
  temperatureDashboard: TemperatureDashboard;
};
