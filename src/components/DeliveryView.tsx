import React, { useState } from 'react';
import { 
  Bike, 
  MapPin, 
  Store, 
  Phone, 
  CheckCircle, 
  Navigation, 
  DollarSign, 
  Award,
  Compass,
  ArrowRight,
  ShieldCheck
} from 'lucide-react';
import { Order, OrderStatus } from '../types';
import { LiveNavRadarView } from './LiveNavRadarView';

interface DeliveryViewProps {
  orders: Order[];
  onUpdateOrderStatus: (orderId: number, newStatus: OrderStatus) => void;
  onUpdateOrderProgress: (orderId: number, progress: number) => void;
}

export const DeliveryView: React.FC<DeliveryViewProps> = ({
  orders,
  onUpdateOrderStatus,
  onUpdateOrderProgress,
}) => {
  const [isOnline, setIsOnline] = useState(true);
  const [activeDeliveryId, setActiveDeliveryId] = useState<number | null>(
    orders.find((o) => o.status === 'DELIVERING' || o.status === 'ASSIGNED')?.id ||
    (orders.length > 0 ? orders[0].id : null)
  );

  const activeOrder = orders.find((o) => o.id === activeDeliveryId) || null;

  const completedOrders = orders.filter((o) => o.status === 'DELIVERED');
  const totalEarnings = completedOrders.length * 25; // 25 delivery commission per trip

  const availablePickups = orders.filter((o) => o.status === 'ACCEPTED');

  return (
    <div className="space-y-6">
      {/* Partner Status Header */}
      <div className="bg-slate-900 rounded-2xl p-5 text-white shadow-xl flex flex-wrap items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="w-12 h-12 rounded-xl bg-sky-500/20 text-sky-400 border border-sky-500/30 flex items-center justify-center font-bold text-xl">
            <Bike className="w-6 h-6" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h2 className="text-lg font-bold">Alex Johnson</h2>
              <span className="px-2 py-0.5 rounded-full text-xs font-semibold bg-sky-950 text-sky-300 border border-sky-800">
                Partner ID: #10
              </span>
            </div>
            <p className="text-xs text-slate-400">Hyperlocal Delivery Fleet • Honda Activa EV (KA-01-EA-4921)</p>
          </div>
        </div>

        <div className="flex items-center gap-4">
          <div className="text-right">
            <span className="text-[11px] text-slate-400 block">Today's Fleet Payout</span>
            <span className="text-xl font-extrabold text-emerald-400">₹{totalEarnings}</span>
          </div>

          <button
            type="button"
            onClick={() => setIsOnline(!isOnline)}
            className={`px-4 py-2 rounded-xl text-xs font-bold transition-all flex items-center gap-2 ${
              isOnline
                ? 'bg-emerald-600 hover:bg-emerald-500 text-white shadow-md'
                : 'bg-slate-800 text-slate-400 border border-slate-700'
            }`}
          >
            <span className={`w-2 h-2 rounded-full ${isOnline ? 'bg-white animate-ping' : 'bg-slate-500'}`} />
            {isOnline ? 'Duty Online' : 'Duty Offline'}
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left column: Orders list */}
        <div className="lg:col-span-5 space-y-4">
          <h3 className="text-xs font-bold text-slate-400 uppercase tracking-wider">
            Assigned & Nearby Deliveries ({orders.length})
          </h3>

          {orders.map((ord) => {
            const isSelected = activeDeliveryId === ord.id;
            return (
              <div
                key={ord.id}
                onClick={() => setActiveDeliveryId(ord.id)}
                className={`p-4 rounded-2xl border transition-all cursor-pointer bg-white text-left ${
                  isSelected
                    ? 'border-sky-600 ring-2 ring-sky-500/20 shadow-md'
                    : 'border-slate-200 hover:border-slate-300'
                }`}
              >
                <div className="flex items-center justify-between mb-2">
                  <div className="flex items-center gap-1.5">
                    <span className="font-bold text-sm text-slate-900">Order #{ord.id}</span>
                    <span className="text-xs text-slate-400">({ord.items.length} items)</span>
                  </div>
                  <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full uppercase ${
                    ord.status === 'DELIVERED'
                      ? 'bg-emerald-100 text-emerald-800'
                      : ord.status === 'DELIVERING'
                      ? 'bg-sky-100 text-sky-800'
                      : ord.status === 'ACCEPTED'
                      ? 'bg-amber-100 text-amber-800'
                      : 'bg-slate-100 text-slate-700'
                  }`}>
                    {ord.status}
                  </span>
                </div>

                <div className="space-y-1.5 text-xs text-slate-600">
                  <div className="flex items-start gap-2">
                    <Store className="w-3.5 h-3.5 text-amber-500 shrink-0 mt-0.5" />
                    <span className="truncate font-medium">{ord.vendorName} ({ord.vendorAddress})</span>
                  </div>
                  <div className="flex items-start gap-2">
                    <MapPin className="w-3.5 h-3.5 text-emerald-500 shrink-0 mt-0.5" />
                    <span className="truncate">{ord.custAddress}</span>
                  </div>
                </div>

                <div className="mt-3 pt-2.5 border-t border-slate-100 flex items-center justify-between text-xs">
                  <span className="font-extrabold text-emerald-700">Trip Fare: ₹25</span>
                  <span className="text-sky-600 font-bold flex items-center gap-1">
                    Select Cockpit <ArrowRight className="w-3.5 h-3.5" />
                  </span>
                </div>
              </div>
            );
          })}
        </div>

        {/* Right column: Active Cockpit with Live Radar */}
        <div className="lg:col-span-7 space-y-5">
          {activeOrder ? (
            <div className="space-y-4">
              <LiveNavRadarView
                vendorName={activeOrder.vendorName}
                vendorAddress={activeOrder.vendorAddress}
                vendorLat={activeOrder.vendorLat}
                vendorLng={activeOrder.vendorLng}
                customerAddress={activeOrder.custAddress}
                customerLat={activeOrder.custLat}
                customerLng={activeOrder.custLng}
                driverName="Alex Johnson (You)"
                driverPhone="9876543210"
                status={activeOrder.status}
                progress={activeOrder.deliveryProgress || 0.4}
                onProgressUpdate={(prog) => onUpdateOrderProgress(activeOrder.id, prog)}
                interactive={true}
              />

              {/* Delivery Action controls */}
              <div className="bg-white rounded-2xl p-5 border border-slate-200 shadow-xs space-y-4">
                <h4 className="font-bold text-slate-900 text-sm">Delivery Action Cockpit</h4>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-xs">
                  <div className="p-3 rounded-xl bg-slate-50 border border-slate-200 space-y-1">
                    <span className="text-slate-400 block font-medium">Customer Contact:</span>
                    <span className="font-bold text-slate-800 text-sm block">{activeOrder.customerName}</span>
                    <a
                      href={`tel:${activeOrder.customerPhone}`}
                      className="inline-flex items-center gap-1 text-emerald-600 font-semibold hover:underline"
                    >
                      <Phone className="w-3 h-3" /> {activeOrder.customerPhone}
                    </a>
                  </div>

                  <div className="p-3 rounded-xl bg-slate-50 border border-slate-200 space-y-1">
                    <span className="text-slate-400 block font-medium">Order Collect Amount:</span>
                    <span className="font-bold text-slate-800 text-sm block">₹{activeOrder.totalPrice}</span>
                    <span className="text-emerald-700 font-semibold block">Prepaid Online via Radar</span>
                  </div>
                </div>

                <div className="pt-2 flex flex-wrap items-center gap-3">
                  {activeOrder.status === 'ACCEPTED' && (
                    <button
                      type="button"
                      onClick={() => onUpdateOrderStatus(activeOrder.id, 'DELIVERING')}
                      className="flex-1 py-3 bg-sky-600 hover:bg-sky-700 text-white rounded-xl font-bold text-xs shadow-md transition-all flex items-center justify-center gap-2 cursor-pointer"
                    >
                      <Navigation className="w-4 h-4" /> Picked Up & Start Navigation
                    </button>
                  )}

                  {activeOrder.status === 'DELIVERING' && (
                    <>
                      <button
                        type="button"
                        onClick={() => {
                          const next = Math.min(1.0, (activeOrder.deliveryProgress || 0.5) + 0.25);
                          onUpdateOrderProgress(activeOrder.id, next);
                          if (next >= 1.0) {
                            onUpdateOrderStatus(activeOrder.id, 'DELIVERED');
                          }
                        }}
                        className="flex-1 py-3 bg-slate-900 hover:bg-slate-800 text-white rounded-xl font-bold text-xs shadow-md transition-all flex items-center justify-center gap-2 cursor-pointer"
                      >
                        <Compass className="w-4 h-4 text-emerald-400" /> Advance GPS Route (+25%)
                      </button>

                      <button
                        type="button"
                        onClick={() => onUpdateOrderStatus(activeOrder.id, 'DELIVERED')}
                        className="py-3 px-6 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl font-bold text-xs shadow-md transition-all flex items-center justify-center gap-2 cursor-pointer"
                      >
                        <CheckCircle className="w-4 h-4" /> Complete Handover
                      </button>
                    </>
                  )}

                  {activeOrder.status === 'DELIVERED' && (
                    <div className="w-full p-3 rounded-xl bg-emerald-50 text-emerald-800 border border-emerald-200 text-xs font-semibold flex items-center justify-between">
                      <span className="flex items-center gap-2">
                        <CheckCircle className="w-4 h-4 text-emerald-600" /> Successfully delivered to customer
                      </span>
                      <span className="font-extrabold">+₹25 Commission Earned</span>
                    </div>
                  )}
                </div>
              </div>
            </div>
          ) : (
            <div className="bg-white rounded-2xl p-8 text-center text-slate-400 border border-slate-200">
              <Bike className="w-12 h-12 mx-auto text-slate-300 stroke-[1.5] mb-2" />
              <p className="font-semibold text-slate-700">Select an order to activate Radar Cockpit</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
