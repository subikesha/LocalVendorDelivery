import React, { useState } from 'react';
import { 
  ShoppingBag, 
  Store, 
  Search, 
  Star, 
  Clock, 
  MapPin, 
  Plus, 
  Minus, 
  Trash2, 
  ArrowRight, 
  CheckCircle2, 
  Sparkles, 
  Compass,
  CreditCard
} from 'lucide-react';
import { Vendor, Product, Order, StoreCategory } from '../types';
import { LiveNavRadarView } from './LiveNavRadarView';

interface CustomerViewProps {
  vendors: Vendor[];
  products: Product[];
  orders: Order[];
  onPlaceOrder: (newOrder: Omit<Order, 'id' | 'orderTime'>) => void;
  onUpdateOrderProgress: (orderId: number, progress: number) => void;
  onSubmitRating: (orderId: number, rating: number, feedback: string) => void;
}

const CATEGORIES: ('All' | StoreCategory)[] = [
  'All',
  'Restaurants & Food',
  'Supermarket & Grocery',
  'Bakery & Sweets',
  'Fresh Fruits & Vegetables',
];

export const CustomerView: React.FC<CustomerViewProps> = ({
  vendors,
  products,
  orders,
  onPlaceOrder,
  onUpdateOrderProgress,
  onSubmitRating,
}) => {
  const [selectedCategory, setSelectedCategory] = useState<'All' | StoreCategory>('All');
  const [searchQuery, setSearchQuery] = useState('');
  const [activeVendorId, setActiveVendorId] = useState<number>(2); // Default Pizza Palace
  const [cart, setCart] = useState<{ product: Product; quantity: number }[]>([]);
  const [customerAddress, setCustomerAddress] = useState('Flat 402, Green Orchid Residency');
  const [customerPhone, setCustomerPhone] = useState('9988776655');
  const [activeTab, setActiveTab] = useState<'browse' | 'tracking'>('browse');
  const [selectedOrderForTracking, setSelectedOrderForTracking] = useState<Order | null>(
    orders.length > 0 ? orders[0] : null
  );

  // Filter vendors
  const filteredVendors = vendors.filter((v) => {
    const matchesCat = selectedCategory === 'All' || v.category === selectedCategory;
    const matchesSearch =
      v.shopName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      v.category.toLowerCase().includes(searchQuery.toLowerCase());
    return matchesCat && matchesSearch;
  });

  const currentVendor = vendors.find((v) => v.id === activeVendorId) || vendors[0];
  const vendorProducts = products.filter((p) => p.vendorId === activeVendorId);

  // Cart operations
  const addToCart = (product: Product) => {
    // If cart has items from another vendor, confirm clear
    if (cart.length > 0 && cart[0].product.vendorId !== product.vendorId) {
      if (
        !window.confirm(
          `Your cart contains items from another store. Replace cart with items from ${currentVendor.shopName}?`
        )
      ) {
        return;
      }
      setCart([{ product, quantity: 1 }]);
      return;
    }

    setCart((prev) => {
      const existing = prev.find((item) => item.product.id === product.id);
      if (existing) {
        return prev.map((item) =>
          item.product.id === product.id
            ? { ...item, quantity: item.quantity + 1 }
            : item
        );
      }
      return [...prev, { product, quantity: 1 }];
    });
  };

  const updateCartQty = (productId: number, delta: number) => {
    setCart((prev) => {
      return prev
        .map((item) => {
          if (item.product.id === productId) {
            const newQty = item.quantity + delta;
            return newQty > 0 ? { ...item, quantity: newQty } : null;
          }
          return item;
        })
        .filter(Boolean) as { product: Product; quantity: number }[];
    });
  };

  const subtotal = cart.reduce((acc, item) => acc + item.product.price * item.quantity, 0);
  const deliveryFee = cart.length > 0 ? 25 : 0;
  const grandTotal = subtotal + deliveryFee;

  const handleCheckout = () => {
    if (cart.length === 0) return;
    const orderData: Omit<Order, 'id' | 'orderTime'> = {
      customerId: 20,
      customerName: 'Priya Sharma',
      customerPhone,
      vendorId: currentVendor.id,
      vendorName: currentVendor.shopName,
      vendorAddress: currentVendor.address,
      vendorLat: currentVendor.latitude,
      vendorLng: currentVendor.longitude,
      status: 'PENDING',
      items: cart.map((c) => ({
        productId: c.product.id,
        productName: c.product.name,
        price: c.product.price,
        quantity: c.quantity,
      })),
      totalPrice: grandTotal,
      custAddress: customerAddress,
      custLat: 12.978,
      custLng: 77.59,
      deliveryFee,
      deliveryProgress: 0.05,
    };

    onPlaceOrder(orderData);
    setCart([]);
    setActiveTab('tracking');
  };

  return (
    <div className="space-y-6">
      {/* Tab Switcher */}
      <div className="flex items-center justify-between border-b border-slate-200 pb-3">
        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={() => setActiveTab('browse')}
            className={`px-4 py-2 rounded-xl text-sm font-semibold transition-all ${
              activeTab === 'browse'
                ? 'bg-emerald-600 text-white shadow-sm'
                : 'text-slate-600 hover:bg-slate-100'
            }`}
          >
            🏬 Explore Neighborhood Stores
          </button>
          <button
            type="button"
            onClick={() => {
              setActiveTab('tracking');
              if (!selectedOrderForTracking && orders.length > 0) {
                setSelectedOrderForTracking(orders[0]);
              }
            }}
            className={`px-4 py-2 rounded-xl text-sm font-semibold transition-all relative ${
              activeTab === 'tracking'
                ? 'bg-emerald-600 text-white shadow-sm'
                : 'text-slate-600 hover:bg-slate-100'
            }`}
          >
            🛵 Live Order Radar & Tracking
            {orders.some((o) => o.status === 'DELIVERING') && (
              <span className="ml-2 inline-flex items-center px-1.5 py-0.5 rounded-full text-[10px] font-bold bg-sky-500 text-white animate-pulse">
                LIVE
              </span>
            )}
          </button>
        </div>

        <div className="hidden sm:flex items-center gap-2 text-xs text-slate-500">
          <MapPin className="w-3.5 h-3.5 text-emerald-600" />
          <span>Delivery to: <strong>{customerAddress}</strong></span>
        </div>
      </div>

      {activeTab === 'browse' ? (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
          {/* Main Store & Product section */}
          <div className="lg:col-span-8 space-y-6">
            {/* Category pills & Search */}
            <div className="space-y-3">
              <div className="relative">
                <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
                <input
                  type="text"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  placeholder="Search stores, pizza, bakery, fresh groceries..."
                  className="w-full pl-10 pr-4 py-2.5 bg-white border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-600 shadow-xs"
                />
              </div>

              <div className="flex items-center gap-2 overflow-x-auto pb-1 scrollbar-none">
                {CATEGORIES.map((cat) => (
                  <button
                    key={cat}
                    type="button"
                    onClick={() => setSelectedCategory(cat)}
                    className={`whitespace-nowrap px-3.5 py-1.5 rounded-full text-xs font-medium transition-all ${
                      selectedCategory === cat
                        ? 'bg-slate-900 text-white shadow-xs'
                        : 'bg-white border border-slate-200 text-slate-600 hover:bg-slate-50'
                    }`}
                  >
                    {cat}
                  </button>
                ))}
              </div>
            </div>

            {/* Vendor Horizontal Cards */}
            <div>
              <h2 className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-3">
                Local Vendors Nearby ({filteredVendors.length})
              </h2>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                {filteredVendors.map((v) => {
                  const isSelected = v.id === activeVendorId;
                  return (
                    <div
                      key={v.id}
                      onClick={() => setActiveVendorId(v.id)}
                      className={`p-4 rounded-2xl border transition-all cursor-pointer bg-white text-left ${
                        isSelected
                          ? 'border-emerald-600 ring-2 ring-emerald-500/20 shadow-md'
                          : 'border-slate-200 hover:border-slate-300 hover:shadow-xs'
                      }`}
                    >
                      <div className="flex items-start justify-between">
                        <div>
                          <span className="text-[11px] font-semibold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded-md">
                            {v.category}
                          </span>
                          <h3 className="font-bold text-slate-900 mt-1.5 text-base">{v.shopName}</h3>
                          <p className="text-xs text-slate-500 flex items-center gap-1 mt-0.5">
                            <MapPin className="w-3 h-3 text-slate-400" /> {v.address}
                          </p>
                        </div>
                        <div className="flex items-center gap-1 bg-amber-50 text-amber-800 px-2 py-1 rounded-lg text-xs font-bold">
                          <Star className="w-3 h-3 fill-amber-400 text-amber-500" />
                          <span>{v.rating}</span>
                        </div>
                      </div>

                      <div className="mt-3 pt-3 border-t border-slate-100 flex items-center justify-between text-xs text-slate-500">
                        <span className="flex items-center gap-1">
                          <Clock className="w-3.5 h-3.5 text-slate-400" /> {v.prepTimeMinutes} mins prep
                        </span>
                        <span className="font-medium text-emerald-600">
                          {isSelected ? 'Currently Viewing Menu' : 'Tap to View Menu →'}
                        </span>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>

            {/* Menu Items for Selected Vendor */}
            <div className="bg-white rounded-2xl p-5 border border-slate-200/80 shadow-xs">
              <div className="flex items-center justify-between mb-4 pb-3 border-b border-slate-100">
                <div>
                  <h3 className="font-bold text-lg text-slate-900">{currentVendor.shopName} Menu</h3>
                  <p className="text-xs text-slate-500">{currentVendor.category} • Fresh direct fulfillment</p>
                </div>
                <span className="text-xs font-medium text-slate-500 bg-slate-100 px-2.5 py-1 rounded-full">
                  {vendorProducts.length} items available
                </span>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                {vendorProducts.map((product) => {
                  const inCart = cart.find((c) => c.product.id === product.id);
                  return (
                    <div
                      key={product.id}
                      className="p-3.5 rounded-xl border border-slate-100 bg-slate-50/60 hover:bg-white hover:border-slate-200 transition-all flex flex-col justify-between"
                    >
                      <div>
                        <div className="flex items-start justify-between gap-2">
                          <h4 className="font-semibold text-sm text-slate-900">{product.name}</h4>
                          <span className="font-extrabold text-slate-900 text-sm whitespace-nowrap">
                            ₹{product.price}
                          </span>
                        </div>
                        <p className="text-xs text-slate-500 mt-1 line-clamp-2 leading-relaxed">
                          {product.description}
                        </p>
                      </div>

                      <div className="mt-3 pt-2.5 border-t border-slate-200/60 flex items-center justify-between">
                        <span className="text-[11px] text-slate-400 font-medium">
                          In stock: {product.quantity}
                        </span>

                        {inCart ? (
                          <div className="flex items-center gap-2 bg-emerald-50 border border-emerald-200 rounded-lg p-1">
                            <button
                              type="button"
                              onClick={() => updateCartQty(product.id, -1)}
                              className="w-6 h-6 rounded bg-white text-emerald-800 flex items-center justify-center shadow-xs hover:bg-emerald-100"
                            >
                              <Minus className="w-3 h-3" />
                            </button>
                            <span className="text-xs font-bold text-emerald-900 px-1">
                              {inCart.quantity}
                            </span>
                            <button
                              type="button"
                              onClick={() => updateCartQty(product.id, 1)}
                              className="w-6 h-6 rounded bg-white text-emerald-800 flex items-center justify-center shadow-xs hover:bg-emerald-100"
                            >
                              <Plus className="w-3 h-3" />
                            </button>
                          </div>
                        ) : (
                          <button
                            type="button"
                            onClick={() => addToCart(product)}
                            className="px-3 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-xs font-semibold flex items-center gap-1.5 shadow-xs transition-colors"
                          >
                            <Plus className="w-3.5 h-3.5" /> Add
                          </button>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          </div>

          {/* Cart & Checkout Sidebar */}
          <div className="lg:col-span-4 space-y-4">
            <div className="bg-white rounded-2xl p-5 border border-slate-200/80 shadow-sm sticky top-6">
              <div className="flex items-center justify-between pb-3 border-b border-slate-100">
                <div className="flex items-center gap-2">
                  <ShoppingBag className="w-5 h-5 text-emerald-600" />
                  <h3 className="font-bold text-slate-900">Your Basket</h3>
                </div>
                <span className="text-xs font-semibold bg-emerald-100 text-emerald-800 px-2 py-0.5 rounded-full">
                  {cart.reduce((sum, item) => sum + item.quantity, 0)} items
                </span>
              </div>

              {cart.length === 0 ? (
                <div className="py-10 text-center text-slate-400 space-y-2">
                  <ShoppingBag className="w-10 h-10 mx-auto text-slate-300 stroke-[1.5]" />
                  <p className="text-xs font-medium">Your basket is empty</p>
                  <p className="text-[11px] text-slate-400">Select items from {currentVendor.shopName} to start ordering</p>
                </div>
              ) : (
                <div className="space-y-4 mt-4">
                  {/* Cart Items List */}
                  <div className="space-y-2.5 max-h-56 overflow-y-auto pr-1">
                    {cart.map(({ product, quantity }) => (
                      <div
                        key={product.id}
                        className="flex items-center justify-between text-xs py-1.5 border-b border-slate-100 last:border-0"
                      >
                        <div className="flex-1 pr-2">
                          <span className="font-semibold text-slate-800 block">{product.name}</span>
                          <span className="text-slate-400 text-[11px]">
                            ₹{product.price} × {quantity}
                          </span>
                        </div>

                        <div className="flex items-center gap-1.5">
                          <button
                            type="button"
                            onClick={() => updateCartQty(product.id, -1)}
                            className="p-1 rounded bg-slate-100 hover:bg-slate-200 text-slate-600"
                          >
                            <Minus className="w-3 h-3" />
                          </button>
                          <span className="w-4 text-center font-bold text-slate-800">{quantity}</span>
                          <button
                            type="button"
                            onClick={() => updateCartQty(product.id, 1)}
                            className="p-1 rounded bg-slate-100 hover:bg-slate-200 text-slate-600"
                          >
                            <Plus className="w-3 h-3" />
                          </button>
                          <span className="font-bold text-slate-900 w-12 text-right">
                            ₹{product.price * quantity}
                          </span>
                        </div>
                      </div>
                    ))}
                  </div>

                  {/* Delivery Location Field */}
                  <div className="pt-3 border-t border-slate-100 space-y-2">
                    <label className="text-xs font-semibold text-slate-700 block">
                      Delivery Address
                    </label>
                    <input
                      type="text"
                      value={customerAddress}
                      onChange={(e) => setCustomerAddress(e.target.value)}
                      className="w-full text-xs px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg text-slate-800 focus:outline-none focus:bg-white"
                      placeholder="Enter flat / house number & street"
                    />

                    <label className="text-xs font-semibold text-slate-700 block mt-2">
                      Customer Phone Number
                    </label>
                    <input
                      type="tel"
                      value={customerPhone}
                      onChange={(e) => setCustomerPhone(e.target.value)}
                      className="w-full text-xs px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg text-slate-800 focus:outline-none focus:bg-white"
                      placeholder="10-digit mobile number"
                    />
                  </div>

                  {/* Pricing Breakdown */}
                  <div className="pt-3 border-t border-slate-100 space-y-1.5 text-xs">
                    <div className="flex justify-between text-slate-600">
                      <span>Item Subtotal</span>
                      <span>₹{subtotal}</span>
                    </div>
                    <div className="flex justify-between text-slate-600">
                      <span>Hyperlocal Delivery Fee</span>
                      <span>₹{deliveryFee}</span>
                    </div>
                    <div className="flex justify-between font-extrabold text-slate-900 text-sm pt-2 border-t border-slate-200">
                      <span>To Pay</span>
                      <span>₹{grandTotal}</span>
                    </div>
                  </div>

                  {/* Place Order CTA */}
                  <button
                    type="button"
                    onClick={handleCheckout}
                    className="w-full py-3 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl font-bold text-sm shadow-md transition-all flex items-center justify-center gap-2 cursor-pointer"
                  >
                    <span>Place Hyperlocal Order</span>
                    <ArrowRight className="w-4 h-4" />
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>
      ) : (
        /* Real-Time Order Radar & Tracking Tab */
        <div className="space-y-6">
          {orders.length === 0 ? (
            <div className="bg-white rounded-2xl p-8 text-center text-slate-500 border border-slate-200">
              <ShoppingBag className="w-12 h-12 mx-auto text-slate-300 stroke-[1.5] mb-2" />
              <h3 className="font-bold text-slate-800">No active orders yet</h3>
              <p className="text-xs text-slate-400 mt-1">Place an order from any neighborhood store to monitor live radar delivery.</p>
              <button
                type="button"
                onClick={() => setActiveTab('browse')}
                className="mt-4 px-4 py-2 bg-emerald-600 text-white rounded-xl text-xs font-bold"
              >
                Start Ordering Now
              </button>
            </div>
          ) : (
            <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
              {/* Order selector list */}
              <div className="lg:col-span-4 space-y-3">
                <h3 className="text-xs font-bold text-slate-400 uppercase tracking-wider">
                  Your Orders ({orders.length})
                </h3>
                {orders.map((ord) => {
                  const isSelected = selectedOrderForTracking?.id === ord.id;
                  return (
                    <div
                      key={ord.id}
                      onClick={() => setSelectedOrderForTracking(ord)}
                      className={`p-4 rounded-2xl border transition-all cursor-pointer bg-white text-left ${
                        isSelected
                          ? 'border-emerald-600 ring-2 ring-emerald-500/20 shadow-md'
                          : 'border-slate-200 hover:border-slate-300'
                      }`}
                    >
                      <div className="flex items-center justify-between mb-1.5">
                        <span className="font-bold text-sm text-slate-900">Order #{ord.id}</span>
                        <span className={`text-[11px] font-bold px-2 py-0.5 rounded-full uppercase ${
                          ord.status === 'DELIVERED'
                            ? 'bg-emerald-100 text-emerald-800'
                            : ord.status === 'DELIVERING'
                            ? 'bg-sky-100 text-sky-800'
                            : 'bg-amber-100 text-amber-800'
                        }`}>
                          {ord.status}
                        </span>
                      </div>
                      <p className="text-xs text-slate-600 font-medium">{ord.vendorName}</p>
                      <p className="text-[11px] text-slate-400 mt-1">
                        {ord.items.length} items • ₹{ord.totalPrice} • {ord.orderTime}
                      </p>
                    </div>
                  );
                })}
              </div>

              {/* Active Order Live Radar Details */}
              <div className="lg:col-span-8 space-y-5">
                {selectedOrderForTracking && (
                  <>
                    <LiveNavRadarView
                      vendorName={selectedOrderForTracking.vendorName}
                      vendorAddress={selectedOrderForTracking.vendorAddress}
                      vendorLat={selectedOrderForTracking.vendorLat}
                      vendorLng={selectedOrderForTracking.vendorLng}
                      customerAddress={selectedOrderForTracking.custAddress}
                      customerLat={selectedOrderForTracking.custLat}
                      customerLng={selectedOrderForTracking.custLng}
                      driverName={selectedOrderForTracking.deliveryPartnerName || 'Alex Johnson (Assigned)'}
                      driverPhone={selectedOrderForTracking.deliveryPartnerPhone || '9876543210'}
                      status={selectedOrderForTracking.status}
                      progress={selectedOrderForTracking.deliveryProgress || 0.4}
                      onProgressUpdate={(newProg) =>
                        onUpdateOrderProgress(selectedOrderForTracking.id, newProg)
                      }
                      interactive={true}
                    />

                    {/* Order summary card */}
                    <div className="bg-white rounded-2xl p-5 border border-slate-200 shadow-xs space-y-4">
                      <div className="flex items-center justify-between pb-3 border-b border-slate-100">
                        <div>
                          <h4 className="font-bold text-slate-900">Order #{selectedOrderForTracking.id} Details</h4>
                          <p className="text-xs text-slate-500">Ordered from {selectedOrderForTracking.vendorName}</p>
                        </div>
                        <span className="text-sm font-extrabold text-slate-900">
                          Total: ₹{selectedOrderForTracking.totalPrice}
                        </span>
                      </div>

                      <div className="space-y-2">
                        {selectedOrderForTracking.items.map((item, idx) => (
                          <div key={idx} className="flex justify-between text-xs text-slate-600">
                            <span>
                              {item.quantity}x {item.productName}
                            </span>
                            <span className="font-medium text-slate-900">₹{item.price * item.quantity}</span>
                          </div>
                        ))}
                      </div>

                      {/* Post-delivery feedback rating if delivered */}
                      {selectedOrderForTracking.status === 'DELIVERED' && (
                        <div className="pt-4 border-t border-slate-100 bg-emerald-50/50 p-4 rounded-xl space-y-3">
                          <div className="flex items-center gap-2 text-emerald-800 font-semibold text-xs">
                            <CheckCircle2 className="w-4 h-4 text-emerald-600" />
                            <span>Delivery Completed! How was your experience?</span>
                          </div>
                          <div className="flex items-center gap-1.5">
                            {[1, 2, 3, 4, 5].map((star) => (
                              <button
                                key={star}
                                type="button"
                                onClick={() => onSubmitRating(selectedOrderForTracking.id, star, 'Great prompt delivery!')}
                                className="p-1 hover:scale-110 transition-transform"
                              >
                                <Star
                                  className={`w-6 h-6 ${
                                    (selectedOrderForTracking.rating || 0) >= star
                                      ? 'fill-amber-400 text-amber-500'
                                      : 'text-slate-300'
                                  }`}
                                />
                              </button>
                            ))}
                            <span className="text-xs text-slate-500 ml-2">
                              {selectedOrderForTracking.rating
                                ? `${selectedOrderForTracking.rating} Stars Rated`
                                : 'Tap stars to rate'}
                            </span>
                          </div>
                        </div>
                      )}
                    </div>
                  </>
                )}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
};
