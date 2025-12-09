
export type WarehouseDashboard = {
  warehouseName: string;
  totalCapacity: number;
  usedCapacity: number;
  remainingCapacity: number;
  utilisationPercent: number;
  ambient: number;
  cold: number;
  freeze: number;
};
