export type UserRole = 'CUSTOMER' | 'VENDOR' | 'DELIVERY' | 'ADMIN';

export type StoreCategory = 
  | 'Restaurants & Food'
  | 'Supermarket & Grocery'
  | 'Bakery & Sweets'
  | 'Fresh Fruits & Vegetables'
  | 'Pharmacy & Healthcare';

export type OrderStatus = 'PENDING' | 'ACCEPTED' | 'ASSIGNED' | 'DELIVERING' | 'DELIVERED' | 'REJECTED';

export interface User {
  id: number;
  username: string;
  fullName: string;
  phone: string;
  role: UserRole;
  address?: string;
  lat?: number;
  lng?: number;
  latitude?: number;
  longitude?: number;
}

export interface Vendor {
  id: number;
  userId: number;
  shopName: string;
  category: StoreCategory;
  address: string;
  latitude: number;
  longitude: number;
  rating: number;
  prepTimeMinutes: number;
  isOpen: boolean;
  bannerImage?: string;
}

export interface Product {
  id: number;
  vendorId: number;
  name: string;
  category: string;
  price: number;
  quantity: number;
  imageCode?: string;
  description?: string;
}

export interface OrderItem {
  productId: number;
  productName: string;
  price: number;
  quantity: number;
}

export interface Order {
  id: number;
  customerId: number;
  customerName: string;
  customerPhone: string;
  vendorId: number;
  vendorName: string;
  vendorAddress: string;
  vendorLat: number;
  vendorLng: number;
  deliveryPartnerId?: number;
  deliveryPartnerName?: string;
  deliveryPartnerPhone?: string;
  totalPrice: number;
  deliveryFee: number;
  status: OrderStatus;
  orderTime: string;
  custAddress: string;
  custLat: number;
  custLng: number;
  items: OrderItem[];
  deliveryProgress?: number; // 0.0 to 1.0 interpolation for live radar
  driverLat?: number;
  driverLng?: number;
  rating?: number;
  feedback?: string;
}
