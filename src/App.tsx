import React, { useState, useEffect } from 'react';
import { 
  ShoppingBag, 
  Store, 
  Bike, 
  ShieldCheck, 
  RotateCcw,
  Radio,
  MapPin,
  Sparkles
} from 'lucide-react';
import { UserRole, Vendor, Product, Order, OrderStatus } from './types';
import { INITIAL_USERS, INITIAL_VENDORS, INITIAL_PRODUCTS, INITIAL_ORDERS } from './data/initialData';
import { CustomerView } from './components/CustomerView';
import { VendorView } from './components/VendorView';
import { DeliveryView } from './components/DeliveryView';
import { AdminView } from './components/AdminView';

export function App() {
  const [currentRole, setCurrentRole] = useState<UserRole>('CUSTOMER');

  // Persistence in localStorage
  const [vendors, setVendors] = useState<Vendor[]>(() => {
    const saved = localStorage.getItem('lvd_vendors');
    return saved ? JSON.parse(saved) : INITIAL_VENDORS;
  });

  const [products, setProducts] = useState<Product[]>(() => {
    const saved = localStorage.getItem('lvd_products');
    return saved ? JSON.parse(saved) : INITIAL_PRODUCTS;
  });

  const [orders, setOrders] = useState<Order[]>(() => {
    const saved = localStorage.getItem('lvd_orders');
    return saved ? JSON.parse(saved) : INITIAL_ORDERS;
  });

  useEffect(() => {
    localStorage.setItem('lvd_vendors', JSON.stringify(vendors));
  }, [vendors]);

  useEffect(() => {
    localStorage.setItem('lvd_products', JSON.stringify(products));
  }, [products]);

  useEffect(() => {
    localStorage.setItem('lvd_orders', JSON.stringify(orders));
  }, [orders]);

  // Order Handlers
  const handlePlaceOrder = (newOrderData: Omit<Order, 'id' | 'orderTime'>) => {
    const newOrder: Order = {
      ...newOrderData,
      id: Math.floor(100 + Math.random() * 900),
      orderTime: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    };
    setOrders((prev) => [newOrder, ...prev]);
  };

  const handleUpdateOrderStatus = (orderId: number, newStatus: OrderStatus) => {
    setOrders((prev) =>
      prev.map((o) => {
        if (o.id === orderId) {
          const updates: Partial<Order> = { status: newStatus };
          if (newStatus === 'DELIVERING' && !o.deliveryPartnerId) {
            updates.deliveryPartnerId = 10;
            updates.deliveryPartnerName = 'Alex Johnson';
            updates.deliveryPartnerPhone = '9876543210';
            updates.deliveryProgress = 0.2;
          }
          if (newStatus === 'DELIVERED') {
            updates.deliveryProgress = 1.0;
          }
          return { ...o, ...updates };
        }
        return o;
      })
    );
  };

  const handleUpdateOrderProgress = (orderId: number, progress: number) => {
    setOrders((prev) =>
      prev.map((o) => {
        if (o.id === orderId) {
          const isFinished = progress >= 1.0;
          return {
            ...o,
            deliveryProgress: progress,
            status: isFinished ? 'DELIVERED' : o.status,
          };
        }
        return o;
      })
    );
  };

  const handleSubmitRating = (orderId: number, rating: number, feedback: string) => {
    setOrders((prev) =>
      prev.map((o) => (o.id === orderId ? { ...o, rating, feedback } : o))
    );
  };

  // Vendor Handlers
  const handleAddProduct = (newProduct: Omit<Product, 'id'>) => {
    const product: Product = {
      ...newProduct,
      id: Math.floor(100 + Math.random() * 9000),
    };
    setProducts((prev) => [...prev, product]);
  };

  const handleUpdateProduct = (productId: number, updates: Partial<Product>) => {
    setProducts((prev) =>
      prev.map((p) => (p.id === productId ? { ...p, ...updates } : p))
    );
  };

  // Admin Handlers
  const handleRegisterVendor = (vendorData: Omit<Vendor, 'id' | 'rating' | 'isOpen'>) => {
    const newVendor: Vendor = {
      ...vendorData,
      id: Math.floor(10 + Math.random() * 90),
      rating: 4.8,
      isOpen: true,
    };
    setVendors((prev) => [...prev, newVendor]);
  };

  const handleRegisterDeliveryPartner = (partner: { fullName: string; phone: string; address: string; lat: number; lng: number }) => {
    alert(`Courier partner "${partner.fullName}" registered successfully with phone ${partner.phone}!`);
  };

  const handleResetDemoData = () => {
    if (window.confirm('Reset all demo data back to default state?')) {
      localStorage.removeItem('lvd_vendors');
      localStorage.removeItem('lvd_products');
      localStorage.removeItem('lvd_orders');
      setVendors(INITIAL_VENDORS);
      setProducts(INITIAL_PRODUCTS);
      setOrders(INITIAL_ORDERS);
    }
  };

  // Active counts for role badges
  const pendingVendorOrders = orders.filter((o) => o.status === 'PENDING').length;
  const activeDeliveries = orders.filter((o) => o.status === 'DELIVERING' || o.status === 'ACCEPTED').length;

  return (
    <div className="min-h-screen bg-[#F8F9FA] text-[#1E293B] flex flex-col">
      {/* Top Navigation Bar */}
      <header className="bg-white border-b border-slate-200/80 sticky top-0 z-40 shadow-xs">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            {/* App Brand */}
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-emerald-600 text-white flex items-center justify-center font-extrabold text-xl shadow-md">
                🛵
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <h1 className="text-base sm:text-lg font-bold text-slate-900 leading-none">
                    Local Vendor Delivery
                  </h1>
                  <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-bold bg-emerald-50 text-emerald-700 border border-emerald-200">
                    <Radio className="w-2.5 h-2.5 text-emerald-600 animate-pulse" /> Radar Active
                  </span>
                </div>
                <p className="text-[11px] text-slate-500 mt-0.5">Hyperlocal Neighborhood Fulfillment Ecosystem</p>
              </div>
            </div>

            {/* Role Switcher Pills */}
            <div className="flex items-center bg-slate-100 p-1 rounded-2xl border border-slate-200 text-xs font-semibold">
              <button
                type="button"
                onClick={() => setCurrentRole('CUSTOMER')}
                className={`px-3 sm:px-4 py-1.5 rounded-xl transition-all flex items-center gap-1.5 cursor-pointer ${
                  currentRole === 'CUSTOMER'
                    ? 'bg-white text-slate-900 shadow-xs font-bold'
                    : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                <ShoppingBag className="w-3.5 h-3.5 text-emerald-600" />
                <span className="hidden sm:inline">Customer</span>
              </button>

              <button
                type="button"
                onClick={() => setCurrentRole('VENDOR')}
                className={`px-3 sm:px-4 py-1.5 rounded-xl transition-all flex items-center gap-1.5 cursor-pointer relative ${
                  currentRole === 'VENDOR'
                    ? 'bg-white text-slate-900 shadow-xs font-bold'
                    : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                <Store className="w-3.5 h-3.5 text-amber-500" />
                <span className="hidden sm:inline">Vendor</span>
                {pendingVendorOrders > 0 && (
                  <span className="w-2 h-2 rounded-full bg-amber-500 animate-ping" />
                )}
              </button>

              <button
                type="button"
                onClick={() => setCurrentRole('DELIVERY')}
                className={`px-3 sm:px-4 py-1.5 rounded-xl transition-all flex items-center gap-1.5 cursor-pointer relative ${
                  currentRole === 'DELIVERY'
                    ? 'bg-white text-slate-900 shadow-xs font-bold'
                    : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                <Bike className="w-3.5 h-3.5 text-sky-500" />
                <span className="hidden sm:inline">Rider</span>
                {activeDeliveries > 0 && (
                  <span className="w-2 h-2 rounded-full bg-sky-500" />
                )}
              </button>

              <button
                type="button"
                onClick={() => setCurrentRole('ADMIN')}
                className={`px-3 sm:px-4 py-1.5 rounded-xl transition-all flex items-center gap-1.5 cursor-pointer ${
                  currentRole === 'ADMIN'
                    ? 'bg-white text-slate-900 shadow-xs font-bold'
                    : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                <ShieldCheck className="w-3.5 h-3.5 text-indigo-500" />
                <span className="hidden sm:inline">Admin</span>
              </button>
            </div>

            {/* Reset Data button */}
            <div className="hidden md:flex items-center gap-2">
              <button
                type="button"
                onClick={handleResetDemoData}
                title="Reset sample data"
                className="px-2.5 py-1.5 text-xs text-slate-500 hover:text-slate-800 hover:bg-slate-100 rounded-lg flex items-center gap-1 transition-colors"
              >
                <RotateCcw className="w-3 h-3" />
                <span>Reset Demo</span>
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-6">
        {currentRole === 'CUSTOMER' && (
          <CustomerView
            vendors={vendors}
            products={products}
            orders={orders}
            onPlaceOrder={handlePlaceOrder}
            onUpdateOrderProgress={handleUpdateOrderProgress}
            onSubmitRating={handleSubmitRating}
          />
        )}

        {currentRole === 'VENDOR' && (
          <VendorView
            vendors={vendors}
            products={products}
            orders={orders}
            onUpdateOrderStatus={handleUpdateOrderStatus}
            onAddProduct={handleAddProduct}
            onUpdateProduct={handleUpdateProduct}
          />
        )}

        {currentRole === 'DELIVERY' && (
          <DeliveryView
            orders={orders}
            onUpdateOrderStatus={handleUpdateOrderStatus}
            onUpdateOrderProgress={handleUpdateOrderProgress}
          />
        )}

        {currentRole === 'ADMIN' && (
          <AdminView
            vendors={vendors}
            products={products}
            orders={orders}
            onRegisterVendor={handleRegisterVendor}
            onRegisterDeliveryPartner={handleRegisterDeliveryPartner}
          />
        )}
      </main>

      {/* Footer */}
      <footer className="bg-white border-t border-slate-200 text-xs text-slate-500 py-4 mt-auto">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-wrap items-center justify-between gap-2">
          <div className="flex items-center gap-2">
            <span className="font-semibold text-slate-800">Local Vendor Delivery</span>
            <span>•</span>
            <span>Live Radar Route Telemetry Engine</span>
          </div>
          <div className="flex items-center gap-4 text-slate-400">
            <span>Customer</span>
            <span>•</span>
            <span>Vendor</span>
            <span>•</span>
            <span>Delivery Courier</span>
            <span>•</span>
            <span>Operations Admin</span>
          </div>
        </div>
      </footer>
    </div>
  );
}
export default App;
