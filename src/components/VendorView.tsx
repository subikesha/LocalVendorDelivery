import React, { useState } from 'react';
import { 
  Store, 
  Package, 
  Clock, 
  CheckCircle, 
  XCircle, 
  Plus, 
  DollarSign, 
  TrendingUp, 
  AlertCircle,
  Truck,
  Layers,
  Sparkles
} from 'lucide-react';
import { Vendor, Product, Order, OrderStatus } from '../types';

interface VendorViewProps {
  vendors: Vendor[];
  products: Product[];
  orders: Order[];
  onUpdateOrderStatus: (orderId: number, newStatus: OrderStatus) => void;
  onAddProduct: (newProduct: Omit<Product, 'id'>) => void;
  onUpdateProduct: (productId: number, updates: Partial<Product>) => void;
}

export const VendorView: React.FC<VendorViewProps> = ({
  vendors,
  products,
  orders,
  onUpdateOrderStatus,
  onAddProduct,
  onUpdateProduct,
}) => {
  const [selectedVendorId, setSelectedVendorId] = useState<number>(2); // Pizza Palace by default
  const [activeSubTab, setActiveSubTab] = useState<'orders' | 'inventory' | 'analytics'>('orders');

  // New product form state
  const [showAddModal, setShowAddModal] = useState(false);
  const [newProdName, setNewProdName] = useState('');
  const [newProdCat, setNewProdCat] = useState('Food');
  const [newProdPrice, setNewProdPrice] = useState('120');
  const [newProdQty, setNewProdQty] = useState('50');
  const [newProdDesc, setNewProdDesc] = useState('');

  const currentVendor = vendors.find((v) => v.id === selectedVendorId) || vendors[0];
  const vendorOrders = orders.filter((o) => o.vendorId === selectedVendorId);
  const vendorProducts = products.filter((p) => p.vendorId === selectedVendorId);

  const totalRevenue = vendorOrders
    .filter((o) => o.status === 'DELIVERED')
    .reduce((acc, o) => acc + (o.totalPrice - o.deliveryFee), 0);

  const pendingOrders = vendorOrders.filter((o) => o.status === 'PENDING');
  const preparingOrders = vendorOrders.filter((o) => o.status === 'ACCEPTED');

  const handleCreateProduct = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newProdName.trim()) return;

    onAddProduct({
      vendorId: currentVendor.id,
      name: newProdName,
      category: newProdCat,
      price: parseFloat(newProdPrice) || 50,
      quantity: parseInt(newProdQty) || 10,
      imageCode: 'general',
      description: newProdDesc || 'Freshly prepared item',
    });

    setNewProdName('');
    setNewProdDesc('');
    setShowAddModal(false);
  };

  return (
    <div className="space-y-6">
      {/* Store switcher header */}
      <div className="bg-white rounded-2xl p-5 border border-slate-200/80 shadow-xs flex flex-wrap items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="w-12 h-12 rounded-xl bg-amber-500/10 text-amber-600 flex items-center justify-center font-bold text-xl">
            🏬
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h2 className="text-xl font-bold text-slate-900">{currentVendor.shopName}</h2>
              <span className="px-2 py-0.5 rounded-full text-xs font-semibold bg-emerald-100 text-emerald-800">
                Online & Accepting Orders
              </span>
            </div>
            <p className="text-xs text-slate-500">{currentVendor.category} • {currentVendor.address}</p>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <label className="text-xs text-slate-500 font-medium">Switch Vendor Store:</label>
          <select
            value={selectedVendorId}
            onChange={(e) => setSelectedVendorId(Number(e.target.value))}
            className="text-xs font-semibold bg-slate-50 border border-slate-300 rounded-xl px-3 py-2 text-slate-800 focus:outline-none focus:ring-2 focus:ring-emerald-500/20"
          >
            {vendors.map((v) => (
              <option key={v.id} value={v.id}>
                {v.shopName} ({v.category})
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Metrics Row */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div className="bg-white rounded-2xl p-4 border border-slate-200 shadow-xs">
          <span className="text-xs text-slate-500 font-medium block">Total Store Sales</span>
          <div className="flex items-baseline gap-1 mt-1">
            <span className="text-2xl font-extrabold text-slate-900">₹{totalRevenue}</span>
          </div>
          <span className="text-[11px] text-emerald-600 font-medium mt-1 block flex items-center gap-1">
            <TrendingUp className="w-3 h-3" /> Settled to account
          </span>
        </div>

        <div className="bg-white rounded-2xl p-4 border border-slate-200 shadow-xs">
          <span className="text-xs text-slate-500 font-medium block">Incoming Orders</span>
          <span className="text-2xl font-extrabold text-amber-600 mt-1 block">
            {pendingOrders.length}
          </span>
          <span className="text-[11px] text-slate-400 mt-1 block">Action required</span>
        </div>

        <div className="bg-white rounded-2xl p-4 border border-slate-200 shadow-xs">
          <span className="text-xs text-slate-500 font-medium block">Kitchen In Prep</span>
          <span className="text-2xl font-extrabold text-sky-600 mt-1 block">
            {preparingOrders.length}
          </span>
          <span className="text-[11px] text-slate-400 mt-1 block">Cooking / packaging</span>
        </div>

        <div className="bg-white rounded-2xl p-4 border border-slate-200 shadow-xs">
          <span className="text-xs text-slate-500 font-medium block">Menu Items</span>
          <span className="text-2xl font-extrabold text-slate-900 mt-1 block">
            {vendorProducts.length}
          </span>
          <span className="text-[11px] text-slate-400 mt-1 block">Live in customer catalog</span>
        </div>
      </div>

      {/* Sub Tabs */}
      <div className="flex items-center gap-2 border-b border-slate-200 pb-2">
        <button
          type="button"
          onClick={() => setActiveSubTab('orders')}
          className={`px-4 py-2 rounded-xl text-xs font-bold transition-all ${
            activeSubTab === 'orders'
              ? 'bg-slate-900 text-white shadow-xs'
              : 'text-slate-600 hover:bg-slate-100'
          }`}
        >
          Orders Queue ({vendorOrders.length})
        </button>
        <button
          type="button"
          onClick={() => setActiveSubTab('inventory')}
          className={`px-4 py-2 rounded-xl text-xs font-bold transition-all ${
            activeSubTab === 'inventory'
              ? 'bg-slate-900 text-white shadow-xs'
              : 'text-slate-600 hover:bg-slate-100'
          }`}
        >
          Product Catalog & Stock ({vendorProducts.length})
        </button>
      </div>

      {activeSubTab === 'orders' ? (
        /* Orders Queue */
        <div className="space-y-4">
          {vendorOrders.length === 0 ? (
            <div className="bg-white rounded-2xl p-10 text-center text-slate-400 border border-slate-200">
              <Package className="w-12 h-12 mx-auto text-slate-300 stroke-[1.5] mb-2" />
              <p className="font-semibold text-slate-700">No customer orders for this store yet</p>
              <p className="text-xs text-slate-400 mt-1">Switch to Customer tab to simulate placing an order from {currentVendor.shopName}.</p>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {vendorOrders.map((ord) => (
                <div
                  key={ord.id}
                  className="bg-white rounded-2xl p-5 border border-slate-200 shadow-xs space-y-4"
                >
                  <div className="flex items-start justify-between">
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="font-bold text-slate-900">Order #{ord.id}</span>
                        <span className={`text-[10px] font-extrabold px-2 py-0.5 rounded-full uppercase ${
                          ord.status === 'PENDING'
                            ? 'bg-amber-100 text-amber-800'
                            : ord.status === 'ACCEPTED'
                            ? 'bg-sky-100 text-sky-800'
                            : ord.status === 'DELIVERING'
                            ? 'bg-indigo-100 text-indigo-800'
                            : 'bg-emerald-100 text-emerald-800'
                        }`}>
                          {ord.status}
                        </span>
                      </div>
                      <p className="text-xs text-slate-500 mt-1">
                        Customer: <strong className="text-slate-700">{ord.customerName}</strong> ({ord.customerPhone})
                      </p>
                      <p className="text-[11px] text-slate-400 mt-0.5">{ord.custAddress}</p>
                    </div>

                    <span className="font-extrabold text-slate-900 text-base">
                      ₹{ord.totalPrice - ord.deliveryFee}
                    </span>
                  </div>

                  {/* Order Items */}
                  <div className="p-3 bg-slate-50 rounded-xl space-y-1 text-xs">
                    {ord.items.map((item, idx) => (
                      <div key={idx} className="flex justify-between text-slate-700">
                        <span>{item.quantity} × {item.productName}</span>
                        <span className="font-medium">₹{item.price * item.quantity}</span>
                      </div>
                    ))}
                  </div>

                  {/* Vendor Action Buttons */}
                  <div className="flex items-center justify-between pt-2 border-t border-slate-100 text-xs">
                    <span className="text-slate-400 text-[11px]">{ord.orderTime}</span>

                    <div className="flex items-center gap-2">
                      {ord.status === 'PENDING' && (
                        <>
                          <button
                            type="button"
                            onClick={() => onUpdateOrderStatus(ord.id, 'REJECTED')}
                            className="px-3 py-1.5 rounded-lg border border-rose-200 text-rose-600 hover:bg-rose-50 font-semibold flex items-center gap-1 cursor-pointer"
                          >
                            <XCircle className="w-3.5 h-3.5" /> Reject
                          </button>
                          <button
                            type="button"
                            onClick={() => onUpdateOrderStatus(ord.id, 'ACCEPTED')}
                            className="px-3.5 py-1.5 rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white font-semibold flex items-center gap-1 shadow-xs cursor-pointer"
                          >
                            <CheckCircle className="w-3.5 h-3.5" /> Accept Order
                          </button>
                        </>
                      )}

                      {ord.status === 'ACCEPTED' && (
                        <button
                          type="button"
                          onClick={() => onUpdateOrderStatus(ord.id, 'DELIVERING')}
                          className="px-3.5 py-1.5 rounded-lg bg-sky-600 hover:bg-sky-700 text-white font-semibold flex items-center gap-1 shadow-xs cursor-pointer"
                        >
                          <Truck className="w-3.5 h-3.5" /> Ready for Delivery
                        </button>
                      )}

                      {ord.status === 'DELIVERING' && (
                        <span className="text-sky-700 font-semibold flex items-center gap-1">
                          <Truck className="w-3.5 h-3.5 animate-bounce" /> Out for Delivery
                        </span>
                      )}

                      {ord.status === 'DELIVERED' && (
                        <span className="text-emerald-700 font-semibold flex items-center gap-1">
                          <CheckCircle className="w-3.5 h-3.5" /> Completed
                        </span>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      ) : (
        /* Inventory Catalog */
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-xs font-bold text-slate-400 uppercase tracking-wider">
              {currentVendor.shopName} Catalog
            </h3>
            <button
              type="button"
              onClick={() => setShowAddModal(true)}
              className="px-3.5 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-bold flex items-center gap-1.5 shadow-xs cursor-pointer"
            >
              <Plus className="w-4 h-4" /> Add New Item
            </button>
          </div>

          <div className="bg-white rounded-2xl border border-slate-200 overflow-hidden shadow-xs">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 text-slate-500 font-bold border-b border-slate-200">
                <tr>
                  <th className="p-3.5">Product Name</th>
                  <th className="p-3.5">Category</th>
                  <th className="p-3.5">Price (₹)</th>
                  <th className="p-3.5">Available Stock</th>
                  <th className="p-3.5 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {vendorProducts.map((p) => (
                  <tr key={p.id} className="hover:bg-slate-50/70">
                    <td className="p-3.5 font-semibold text-slate-900">
                      {p.name}
                      <p className="text-[11px] text-slate-400 font-normal truncate max-w-xs">{p.description}</p>
                    </td>
                    <td className="p-3.5 text-slate-600">{p.category}</td>
                    <td className="p-3.5 font-bold text-slate-900">₹{p.price}</td>
                    <td className="p-3.5">
                      <span className={`px-2 py-0.5 rounded-md font-bold text-[11px] ${
                        p.quantity > 10 ? 'bg-emerald-50 text-emerald-700' : 'bg-rose-50 text-rose-700'
                      }`}>
                        {p.quantity} in stock
                      </span>
                    </td>
                    <td className="p-3.5 text-right space-x-1.5">
                      <button
                        type="button"
                        onClick={() => onUpdateProduct(p.id, { quantity: p.quantity + 10 })}
                        className="px-2.5 py-1 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-md font-semibold cursor-pointer"
                      >
                        +10 Stock
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Add Product Modal */}
      {showAddModal && (
        <div className="fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-2xl border border-slate-100 space-y-4">
            <div className="flex items-center justify-between pb-2 border-b border-slate-100">
              <h3 className="font-bold text-base text-slate-900">Add Item to {currentVendor.shopName}</h3>
              <button
                type="button"
                onClick={() => setShowAddModal(false)}
                className="text-slate-400 hover:text-slate-600 font-bold text-lg"
              >
                ✕
              </button>
            </div>

            <form onSubmit={handleCreateProduct} className="space-y-3 text-xs">
              <div>
                <label className="font-semibold text-slate-700 block mb-1">Product Title</label>
                <input
                  type="text"
                  required
                  value={newProdName}
                  onChange={(e) => setNewProdName(e.target.value)}
                  placeholder="e.g. Farm Fresh Paneer 200g"
                  className="w-full px-3 py-2 border border-slate-300 rounded-xl focus:ring-2 focus:ring-emerald-500/20 focus:outline-none"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Category</label>
                  <input
                    type="text"
                    value={newProdCat}
                    onChange={(e) => setNewProdCat(e.target.value)}
                    className="w-full px-3 py-2 border border-slate-300 rounded-xl focus:ring-2 focus:ring-emerald-500/20 focus:outline-none"
                  />
                </div>
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Price (₹)</label>
                  <input
                    type="number"
                    required
                    value={newProdPrice}
                    onChange={(e) => setNewProdPrice(e.target.value)}
                    className="w-full px-3 py-2 border border-slate-300 rounded-xl focus:ring-2 focus:ring-emerald-500/20 focus:outline-none"
                  />
                </div>
              </div>

              <div>
                <label className="font-semibold text-slate-700 block mb-1">Initial Stock Count</label>
                <input
                  type="number"
                  value={newProdQty}
                  onChange={(e) => setNewProdQty(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-300 rounded-xl focus:ring-2 focus:ring-emerald-500/20 focus:outline-none"
                />
              </div>

              <div>
                <label className="font-semibold text-slate-700 block mb-1">Description</label>
                <textarea
                  rows={2}
                  value={newProdDesc}
                  onChange={(e) => setNewProdDesc(e.target.value)}
                  placeholder="Ingredients, weight, or cooking notes"
                  className="w-full px-3 py-2 border border-slate-300 rounded-xl focus:ring-2 focus:ring-emerald-500/20 focus:outline-none"
                />
              </div>

              <div className="pt-3 border-t border-slate-100 flex items-center justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setShowAddModal(false)}
                  className="px-4 py-2 border border-slate-300 rounded-xl font-bold text-slate-600 hover:bg-slate-50 cursor-pointer"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-emerald-600 text-white rounded-xl font-bold shadow-xs hover:bg-emerald-700 cursor-pointer"
                >
                  Publish Product
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
