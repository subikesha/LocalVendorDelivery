import React, { useState } from 'react';
import { 
  ShieldCheck, 
  Store, 
  Bike, 
  Package, 
  DollarSign, 
  TrendingUp, 
  UserPlus, 
  MapPin, 
  Plus, 
  CheckCircle,
  Layers,
  Radio
} from 'lucide-react';
import { Vendor, Product, Order, User, StoreCategory } from '../types';

interface AdminViewProps {
  vendors: Vendor[];
  products: Product[];
  orders: Order[];
  onRegisterVendor: (vendor: Omit<Vendor, 'id' | 'rating' | 'isOpen'>) => void;
  onRegisterDeliveryPartner: (partner: { fullName: string; phone: string; address: string; lat: number; lng: number }) => void;
}

const CATEGORIES: StoreCategory[] = [
  'Restaurants & Food',
  'Supermarket & Grocery',
  'Bakery & Sweets',
  'Fresh Fruits & Vegetables',
  'Pharmacy & Healthcare',
];

export const AdminView: React.FC<AdminViewProps> = ({
  vendors,
  products,
  orders,
  onRegisterVendor,
  onRegisterDeliveryPartner,
}) => {
  const [activeTab, setActiveTab] = useState<'overview' | 'vendors' | 'delivery' | 'orders'>('overview');
  const [showVendorModal, setShowVendorModal] = useState(false);
  const [showPartnerModal, setShowPartnerModal] = useState(false);

  // New Vendor Form
  const [shopName, setShopName] = useState('');
  const [shopCategory, setShopCategory] = useState<StoreCategory>('Restaurants & Food');
  const [shopAddress, setShopAddress] = useState('');
  const [shopLat, setShopLat] = useState('12.9730');
  const [shopLng, setShopLng] = useState('77.5940');
  const [prepTime, setPrepTime] = useState('15');

  // New Partner Form
  const [partnerName, setPartnerName] = useState('');
  const [partnerPhone, setPartnerPhone] = useState('');
  const [partnerStation, setPartnerStation] = useState('Central District Depot');

  const totalGMV = orders.reduce((sum, o) => sum + o.totalPrice, 0);
  const totalDelivered = orders.filter((o) => o.status === 'DELIVERED').length;
  const activeDeliveries = orders.filter((o) => o.status === 'DELIVERING' || o.status === 'ACCEPTED').length;

  const handleVendorSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!shopName.trim()) return;

    onRegisterVendor({
      userId: 99,
      shopName,
      category: shopCategory,
      address: shopAddress || 'Main Street, Central Square',
      latitude: parseFloat(shopLat) || 12.973,
      longitude: parseFloat(shopLng) || 77.594,
      prepTimeMinutes: parseInt(prepTime) || 15,
    });

    setShopName('');
    setShopAddress('');
    setShowVendorModal(false);
  };

  const handlePartnerSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!partnerName.trim()) return;

    onRegisterDeliveryPartner({
      fullName: partnerName,
      phone: partnerPhone || '9876543219',
      address: partnerStation,
      lat: 12.972,
      lng: 77.595,
    });

    setPartnerName('');
    setPartnerPhone('');
    setShowPartnerModal(false);
  };

  return (
    <div className="space-y-6">
      {/* Admin Title & Quick Actions */}
      <div className="bg-white rounded-2xl p-5 border border-slate-200/80 shadow-xs flex flex-wrap items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="w-12 h-12 rounded-xl bg-indigo-50 text-indigo-700 flex items-center justify-center font-bold text-xl">
            <ShieldCheck className="w-6 h-6" />
          </div>
          <div>
            <h2 className="text-xl font-bold text-slate-900">City Operations Command Center</h2>
            <p className="text-xs text-slate-500">Hyperlocal multi-vendor orchestration and carrier dispatch</p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={() => setShowVendorModal(true)}
            className="px-3.5 py-2 bg-slate-900 hover:bg-slate-800 text-white rounded-xl text-xs font-bold flex items-center gap-1.5 shadow-xs cursor-pointer"
          >
            <Store className="w-3.5 h-3.5" /> + Register Vendor
          </button>
          <button
            type="button"
            onClick={() => setShowPartnerModal(true)}
            className="px-3.5 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-bold flex items-center gap-1.5 shadow-xs cursor-pointer"
          >
            <Bike className="w-3.5 h-3.5" /> + Register Courier
          </button>
        </div>
      </div>

      {/* Stats Banner */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white rounded-2xl p-4 border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between text-xs text-slate-500 font-medium">
            <span>Registered Stores</span>
            <Store className="w-4 h-4 text-amber-500" />
          </div>
          <span className="text-2xl font-extrabold text-slate-900 mt-2 block">{vendors.length}</span>
          <span className="text-[11px] text-slate-400 mt-0.5 block">Across 5 retail categories</span>
        </div>

        <div className="bg-white rounded-2xl p-4 border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between text-xs text-slate-500 font-medium">
            <span>Total Catalog Products</span>
            <Package className="w-4 h-4 text-indigo-500" />
          </div>
          <span className="text-2xl font-extrabold text-slate-900 mt-2 block">{products.length}</span>
          <span className="text-[11px] text-slate-400 mt-0.5 block">Real-time localized inventory</span>
        </div>

        <div className="bg-white rounded-2xl p-4 border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between text-xs text-slate-500 font-medium">
            <span>Active Deliveries</span>
            <Radio className="w-4 h-4 text-sky-500 animate-pulse" />
          </div>
          <span className="text-2xl font-extrabold text-sky-600 mt-2 block">{activeDeliveries}</span>
          <span className="text-[11px] text-slate-400 mt-0.5 block">Tracking on live radar</span>
        </div>

        <div className="bg-white rounded-2xl p-4 border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between text-xs text-slate-500 font-medium">
            <span>Total Gross GMV</span>
            <DollarSign className="w-4 h-4 text-emerald-500" />
          </div>
          <span className="text-2xl font-extrabold text-slate-900 mt-2 block">₹{totalGMV}</span>
          <span className="text-[11px] text-emerald-600 font-medium mt-0.5 block">
            {totalDelivered} successful deliveries
          </span>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex items-center gap-2 border-b border-slate-200 pb-2">
        <button
          type="button"
          onClick={() => setActiveTab('overview')}
          className={`px-4 py-2 rounded-xl text-xs font-bold transition-all ${
            activeTab === 'overview'
              ? 'bg-slate-900 text-white shadow-xs'
              : 'text-slate-600 hover:bg-slate-100'
          }`}
        >
          Stores Directory ({vendors.length})
        </button>
        <button
          type="button"
          onClick={() => setActiveTab('orders')}
          className={`px-4 py-2 rounded-xl text-xs font-bold transition-all ${
            activeTab === 'orders'
              ? 'bg-slate-900 text-white shadow-xs'
              : 'text-slate-600 hover:bg-slate-100'
          }`}
        >
          All City Orders ({orders.length})
        </button>
      </div>

      {activeTab === 'overview' ? (
        <div className="bg-white rounded-2xl border border-slate-200 overflow-hidden shadow-xs">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-50 text-slate-500 font-bold border-b border-slate-200">
              <tr>
                <th className="p-3.5">Store Details</th>
                <th className="p-3.5">Category</th>
                <th className="p-3.5">Coordinates (Lat, Lng)</th>
                <th className="p-3.5">Products</th>
                <th className="p-3.5">Rating</th>
                <th className="p-3.5 text-right">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {vendors.map((v) => {
                const storeProds = products.filter((p) => p.vendorId === v.id);
                return (
                  <tr key={v.id} className="hover:bg-slate-50/70">
                    <td className="p-3.5 font-semibold text-slate-900">
                      {v.shopName}
                      <p className="text-[11px] text-slate-400 font-normal">{v.address}</p>
                    </td>
                    <td className="p-3.5">
                      <span className="px-2 py-0.5 rounded-md text-[11px] font-semibold bg-slate-100 text-slate-700">
                        {v.category}
                      </span>
                    </td>
                    <td className="p-3.5 text-slate-600 font-mono text-[11px]">
                      {v.latitude.toFixed(4)}, {v.longitude.toFixed(4)}
                    </td>
                    <td className="p-3.5 font-bold text-slate-900">{storeProds.length} items</td>
                    <td className="p-3.5 font-bold text-amber-600">★ {v.rating}</td>
                    <td className="p-3.5 text-right">
                      <span className="px-2 py-0.5 rounded-full text-[10px] font-extrabold bg-emerald-100 text-emerald-800">
                        VERIFIED ACTIVE
                      </span>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      ) : (
        <div className="bg-white rounded-2xl border border-slate-200 overflow-hidden shadow-xs">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-50 text-slate-500 font-bold border-b border-slate-200">
              <tr>
                <th className="p-3.5">Order ID</th>
                <th className="p-3.5">Vendor</th>
                <th className="p-3.5">Customer & Destination</th>
                <th className="p-3.5">Total (₹)</th>
                <th className="p-3.5">Delivery Partner</th>
                <th className="p-3.5 text-right">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {orders.map((ord) => (
                <tr key={ord.id} className="hover:bg-slate-50/70">
                  <td className="p-3.5 font-bold text-slate-900">#{ord.id}</td>
                  <td className="p-3.5 text-slate-700 font-medium">{ord.vendorName}</td>
                  <td className="p-3.5">
                    <span className="font-semibold text-slate-900 block">{ord.customerName}</span>
                    <span className="text-slate-400 text-[11px] truncate block max-w-xs">{ord.custAddress}</span>
                  </td>
                  <td className="p-3.5 font-extrabold text-slate-900">₹{ord.totalPrice}</td>
                  <td className="p-3.5 text-slate-600 font-medium">
                    {ord.deliveryPartnerName || <span className="text-slate-400 italic">Unassigned</span>}
                  </td>
                  <td className="p-3.5 text-right">
                    <span className={`px-2.5 py-1 rounded-full text-[10px] font-extrabold uppercase ${
                      ord.status === 'DELIVERED'
                        ? 'bg-emerald-100 text-emerald-800'
                        : ord.status === 'DELIVERING'
                        ? 'bg-sky-100 text-sky-800'
                        : 'bg-amber-100 text-amber-800'
                    }`}>
                      {ord.status}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Register Vendor Modal */}
      {showVendorModal && (
        <div className="fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-2xl border border-slate-100 space-y-4">
            <div className="flex items-center justify-between pb-2 border-b border-slate-100">
              <h3 className="font-bold text-base text-slate-900">Register New Local Vendor</h3>
              <button
                type="button"
                onClick={() => setShowVendorModal(false)}
                className="text-slate-400 hover:text-slate-600 font-bold text-lg"
              >
                ✕
              </button>
            </div>

            <form onSubmit={handleVendorSubmit} className="space-y-3 text-xs">
              <div>
                <label className="font-semibold text-slate-700 block mb-1">Shop / Business Name</label>
                <input
                  type="text"
                  required
                  value={shopName}
                  onChange={(e) => setShopName(e.target.value)}
                  placeholder="e.g. Apex Organic Market"
                  className="w-full px-3 py-2 border border-slate-300 rounded-xl focus:ring-2 focus:ring-emerald-500/20 focus:outline-none"
                />
              </div>

              <div>
                <label className="font-semibold text-slate-700 block mb-1">Business Category</label>
                <select
                  value={shopCategory}
                  onChange={(e) => setShopCategory(e.target.value as StoreCategory)}
                  className="w-full px-3 py-2 border border-slate-300 rounded-xl focus:ring-2 focus:ring-emerald-500/20 focus:outline-none"
                >
                  {CATEGORIES.map((cat) => (
                    <option key={cat} value={cat}>{cat}</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="font-semibold text-slate-700 block mb-1">Street Address</label>
                <input
                  type="text"
                  required
                  value={shopAddress}
                  onChange={(e) => setShopAddress(e.target.value)}
                  placeholder="e.g. 12 Commerce Boulevard"
                  className="w-full px-3 py-2 border border-slate-300 rounded-xl focus:ring-2 focus:ring-emerald-500/20 focus:outline-none"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Latitude</label>
                  <input
                    type="text"
                    value={shopLat}
                    onChange={(e) => setShopLat(e.target.value)}
                    className="w-full px-3 py-2 border border-slate-300 rounded-xl focus:ring-2 focus:ring-emerald-500/20 focus:outline-none"
                  />
                </div>
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Longitude</label>
                  <input
                    type="text"
                    value={shopLng}
                    onChange={(e) => setShopLng(e.target.value)}
                    className="w-full px-3 py-2 border border-slate-300 rounded-xl focus:ring-2 focus:ring-emerald-500/20 focus:outline-none"
                  />
                </div>
              </div>

              <div className="pt-3 border-t border-slate-100 flex items-center justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setShowVendorModal(false)}
                  className="px-4 py-2 border border-slate-300 rounded-xl font-bold text-slate-600 hover:bg-slate-50 cursor-pointer"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-slate-900 text-white rounded-xl font-bold shadow-xs hover:bg-slate-800 cursor-pointer"
                >
                  Confirm & Provision Store
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Register Partner Modal */}
      {showPartnerModal && (
        <div className="fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-2xl border border-slate-100 space-y-4">
            <div className="flex items-center justify-between pb-2 border-b border-slate-100">
              <h3 className="font-bold text-base text-slate-900">Onboard Delivery Partner</h3>
              <button
                type="button"
                onClick={() => setShowPartnerModal(false)}
                className="text-slate-400 hover:text-slate-600 font-bold text-lg"
              >
                ✕
              </button>
            </div>

            <form onSubmit={handlePartnerSubmit} className="space-y-3 text-xs">
              <div>
                <label className="font-semibold text-slate-700 block mb-1">Partner Full Name</label>
                <input
                  type="text"
                  required
                  value={partnerName}
                  onChange={(e) => setPartnerName(e.target.value)}
                  placeholder="e.g. Vikram Sen"
                  className="w-full px-3 py-2 border border-slate-300 rounded-xl focus:ring-2 focus:ring-emerald-500/20 focus:outline-none"
                />
              </div>

              <div>
                <label className="font-semibold text-slate-700 block mb-1">Mobile Contact</label>
                <input
                  type="tel"
                  required
                  value={partnerPhone}
                  onChange={(e) => setPartnerPhone(e.target.value)}
                  placeholder="10-digit mobile number"
                  className="w-full px-3 py-2 border border-slate-300 rounded-xl focus:ring-2 focus:ring-emerald-500/20 focus:outline-none"
                />
              </div>

              <div>
                <label className="font-semibold text-slate-700 block mb-1">Hub / Assigned Depot</label>
                <input
                  type="text"
                  value={partnerStation}
                  onChange={(e) => setPartnerStation(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-300 rounded-xl focus:ring-2 focus:ring-emerald-500/20 focus:outline-none"
                />
              </div>

              <div className="pt-3 border-t border-slate-100 flex items-center justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setShowPartnerModal(false)}
                  className="px-4 py-2 border border-slate-300 rounded-xl font-bold text-slate-600 hover:bg-slate-50 cursor-pointer"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-emerald-600 text-white rounded-xl font-bold shadow-xs hover:bg-emerald-700 cursor-pointer"
                >
                  Authorize Carrier
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
