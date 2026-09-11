import React, { useEffect, useRef, useState } from 'react';
import { Navigation, Bike, Store, MapPin, Compass, ShieldCheck } from 'lucide-react';

interface LiveNavRadarProps {
  vendorName: string;
  vendorAddress: string;
  vendorLat: number;
  vendorLng: number;
  customerAddress: string;
  customerLat: number;
  customerLng: number;
  driverName?: string;
  driverPhone?: string;
  status: string;
  progress?: number; // 0.0 to 1.0
  onProgressUpdate?: (newProgress: number) => void;
  interactive?: boolean;
}

export const LiveNavRadarView: React.FC<LiveNavRadarProps> = ({
  vendorName,
  vendorAddress,
  vendorLat,
  vendorLng,
  customerAddress,
  customerLat,
  customerLng,
  driverName = 'Alex Johnson (Partner)',
  driverPhone = '9876543210',
  status,
  progress = 0.5,
  onProgressUpdate,
  interactive = true,
}) => {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const [currentProgress, setCurrentProgress] = useState(progress);
  const [isSimulating, setIsSimulating] = useState(status === 'DELIVERING');
  const [viewMode, setViewMode] = useState<'radar' | 'schematic'>('radar');

  useEffect(() => {
    setCurrentProgress(progress);
  }, [progress]);

  // Haversine distance in km
  const calculateDistance = (lat1: number, lon1: number, lat2: number, lon2: number) => {
    const R = 6371; // km
    const dLat = ((lat2 - lat1) * Math.PI) / 180;
    const dLon = ((lon2 - lon1) * Math.PI) / 180;
    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos((lat1 * Math.PI) / 180) *
        Math.cos((lat2 * Math.PI) / 180) *
        Math.sin(dLon / 2) *
        Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
  };

  const totalDistanceKm = Math.max(0.6, calculateDistance(vendorLat, vendorLng, customerLat, customerLng));
  const remainingDistanceKm = Math.max(0, totalDistanceKm * (1 - currentProgress));
  const estimatedMins = Math.max(1, Math.round(remainingDistanceKm * 4 + (1 - currentProgress) * 5));

  // Simulation tick
  useEffect(() => {
    if (!isSimulating || status === 'DELIVERED') return;
    const interval = setInterval(() => {
      setCurrentProgress((prev) => {
        const next = Math.min(1.0, prev + 0.02);
        if (onProgressUpdate) onProgressUpdate(next);
        if (next >= 1.0) setIsSimulating(false);
        return next;
      });
    }, 1000);
    return () => clearInterval(interval);
  }, [isSimulating, status, onProgressUpdate]);

  // Radar Canvas rendering loop
  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    let animationFrameId: number;
    let angle = 0;

    const render = () => {
      const width = canvas.width;
      const height = canvas.height;
      const cx = width / 2;
      const cy = height / 2;
      const maxRadius = Math.min(width, height) * 0.42;

      ctx.clearRect(0, 0, width, height);

      // Background
      ctx.fillStyle = '#0F172A'; // Slate-900 radar night theme
      ctx.fillRect(0, 0, width, height);

      // Radar Concentric rings
      ctx.lineWidth = 1.5;
      const rings = [0.25, 0.5, 0.75, 1.0];
      rings.forEach((ratio) => {
        ctx.beginPath();
        ctx.arc(cx, cy, maxRadius * ratio, 0, Math.PI * 2);
        ctx.strokeStyle = 'rgba(16, 185, 129, 0.2)'; // Emerald glow
        ctx.stroke();
      });

      // Crosshairs
      ctx.beginPath();
      ctx.moveTo(cx - maxRadius, cy);
      ctx.lineTo(cx + maxRadius, cy);
      ctx.moveTo(cx, cy - maxRadius);
      ctx.lineTo(cx, cy + maxRadius);
      ctx.strokeStyle = 'rgba(16, 185, 129, 0.15)';
      ctx.stroke();

      // Radar sweep effect
      angle += 0.03;
      const sweepGradient = ctx.createRadialGradient(cx, cy, 0, cx, cy, maxRadius);
      sweepGradient.addColorStop(0, 'rgba(16, 185, 129, 0.4)');
      sweepGradient.addColorStop(1, 'rgba(16, 185, 129, 0.0)');

      ctx.save();
      ctx.beginPath();
      ctx.moveTo(cx, cy);
      ctx.arc(cx, cy, maxRadius, angle - 0.5, angle);
      ctx.closePath();
      ctx.fillStyle = sweepGradient;
      ctx.fill();
      ctx.restore();

      // Coordinate anchors
      // Vendor Shop at top-left quadrant
      const vendorX = cx - maxRadius * 0.65;
      const vendorY = cy - maxRadius * 0.35;

      // Customer Home at bottom-right quadrant
      const customerX = cx + maxRadius * 0.65;
      const customerY = cy + maxRadius * 0.45;

      // Courier position along interpolated curve with slight realistic arc
      const t = currentProgress;
      const midControlX = cx - 15;
      const midControlY = cy - 40;

      // Quadratic bezier calculation
      const courierX = (1 - t) * (1 - t) * vendorX + 2 * (1 - t) * t * midControlX + t * t * customerX;
      const courierY = (1 - t) * (1 - t) * vendorY + 2 * (1 - t) * t * midControlY + t * t * customerY;

      // Draw Planned Route path
      ctx.beginPath();
      ctx.moveTo(vendorX, vendorY);
      ctx.quadraticCurveTo(midControlX, midControlY, customerX, customerY);
      ctx.setLineDash([6, 6]);
      ctx.lineWidth = 2.5;
      ctx.strokeStyle = 'rgba(148, 163, 184, 0.4)';
      ctx.stroke();
      ctx.setLineDash([]);

      // Draw active covered path
      ctx.beginPath();
      ctx.moveTo(vendorX, vendorY);
      ctx.quadraticCurveTo(
        vendorX + (midControlX - vendorX) * t,
        vendorY + (midControlY - vendorY) * t,
        courierX,
        courierY
      );
      ctx.lineWidth = 3;
      ctx.strokeStyle = '#38BDF8'; // Sky-400
      ctx.stroke();

      // Draw Vendor Node
      ctx.beginPath();
      ctx.arc(vendorX, vendorY, 9, 0, Math.PI * 2);
      ctx.fillStyle = '#F59E0B'; // Amber-500
      ctx.fill();
      ctx.lineWidth = 2;
      ctx.strokeStyle = '#FFFFFF';
      ctx.stroke();

      ctx.font = '11px sans-serif';
      ctx.fillStyle = '#FDE68A';
      ctx.fillText('🏬 Shop', vendorX - 22, vendorY - 14);

      // Draw Customer Node
      ctx.beginPath();
      ctx.arc(customerX, customerY, 9, 0, Math.PI * 2);
      ctx.fillStyle = '#10B981'; // Emerald-500
      ctx.fill();
      ctx.lineWidth = 2;
      ctx.strokeStyle = '#FFFFFF';
      ctx.stroke();

      ctx.fillStyle = '#A7F3D0';
      ctx.fillText('📍 Home', customerX - 20, customerY + 22);

      // Draw Courier / Rider Marker with pulse ring
      const pulse = (Math.sin(Date.now() / 200) + 1) * 6;
      ctx.beginPath();
      ctx.arc(courierX, courierY, 12 + pulse, 0, Math.PI * 2);
      ctx.fillStyle = 'rgba(56, 189, 248, 0.25)';
      ctx.fill();

      ctx.beginPath();
      ctx.arc(courierX, courierY, 10, 0, Math.PI * 2);
      ctx.fillStyle = '#0284C7';
      ctx.fill();
      ctx.lineWidth = 2.5;
      ctx.strokeStyle = '#FFFFFF';
      ctx.stroke();

      ctx.fillStyle = '#E0F2FE';
      ctx.font = 'bold 11px sans-serif';
      ctx.fillText('🛵 Partner', courierX - 25, courierY - 16);

      animationFrameId = requestAnimationFrame(render);
    };

    render();

    return () => {
      cancelAnimationFrame(animationFrameId);
    };
  }, [currentProgress]);

  return (
    <div id="live-nav-radar-card" className="bg-slate-900 rounded-2xl p-5 text-white shadow-xl border border-slate-800">
      {/* Header telemetry */}
      <div className="flex items-center justify-between pb-3 border-b border-slate-800">
        <div className="flex items-center gap-2">
          <div className="relative flex h-3 w-3">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
            <span className="relative inline-flex rounded-full h-3 w-3 bg-emerald-500"></span>
          </div>
          <div>
            <h3 className="font-semibold text-sm tracking-wide text-slate-200">LIVE GPS RADAR NAVIGATOR</h3>
            <p className="text-xs text-slate-400">Autonomous Carrier Precision Tracking</p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <span className={`px-2.5 py-1 text-xs font-semibold rounded-full uppercase tracking-wider ${
            status === 'DELIVERED' 
              ? 'bg-emerald-950 text-emerald-300 border border-emerald-800' 
              : status === 'DELIVERING'
              ? 'bg-sky-950 text-sky-300 border border-sky-800'
              : 'bg-amber-950 text-amber-300 border border-amber-800'
          }`}>
            {status}
          </span>
        </div>
      </div>

      {/* Canvas view */}
      <div className="relative mt-4 flex justify-center items-center rounded-xl overflow-hidden bg-slate-950 border border-slate-800/80">
        <canvas
          ref={canvasRef}
          width={440}
          height={260}
          className="w-full max-w-[480px] h-[240px] block"
        />

        <div className="absolute top-3 right-3 bg-slate-900/85 backdrop-blur-sm border border-slate-700/60 px-3 py-1.5 rounded-lg text-xs flex items-center gap-2 text-slate-300">
          <Compass className="w-3.5 h-3.5 text-emerald-400 animate-spin" style={{ animationDuration: '6s' }} />
          <span>Bearing 42° NE</span>
        </div>

        <div className="absolute bottom-3 left-3 bg-slate-900/90 backdrop-blur-sm border border-slate-700/60 px-3 py-2 rounded-lg text-xs space-y-0.5">
          <div className="text-slate-400">Distance remaining</div>
          <div className="font-bold text-sky-400 text-sm">
            {remainingDistanceKm.toFixed(2)} km <span className="text-slate-400 font-normal">({estimatedMins} mins ETA)</span>
          </div>
        </div>
      </div>

      {/* Route stops detail */}
      <div className="mt-4 grid grid-cols-1 md:grid-cols-2 gap-3 text-xs">
        <div className="p-3 rounded-xl bg-slate-800/50 border border-slate-700/50 flex items-start gap-2.5">
          <Store className="w-4 h-4 text-amber-400 shrink-0 mt-0.5" />
          <div>
            <span className="text-slate-400 block font-medium">Pickup Vendor:</span>
            <span className="font-semibold text-slate-200">{vendorName}</span>
            <p className="text-slate-400 text-[11px] truncate mt-0.5">{vendorAddress}</p>
          </div>
        </div>

        <div className="p-3 rounded-xl bg-slate-800/50 border border-slate-700/50 flex items-start gap-2.5">
          <MapPin className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
          <div>
            <span className="text-slate-400 block font-medium">Dropoff Destination:</span>
            <span className="font-semibold text-slate-200">{customerAddress}</span>
            <p className="text-slate-400 text-[11px] mt-0.5">Lat: {customerLat.toFixed(4)}, Lng: {customerLng.toFixed(4)}</p>
          </div>
        </div>
      </div>

      {/* Driver info & simulation controls */}
      <div className="mt-3 pt-3 border-t border-slate-800 flex flex-wrap items-center justify-between gap-3 text-xs">
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-full bg-sky-900/60 border border-sky-700 flex items-center justify-center text-sky-300 font-bold">
            <Bike className="w-4 h-4" />
          </div>
          <div>
            <div className="font-medium text-slate-200">{driverName}</div>
            <div className="text-slate-400 text-[11px]">Contact: {driverPhone}</div>
          </div>
        </div>

        {interactive && (
          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => setIsSimulating(!isSimulating)}
              className={`px-3 py-1.5 rounded-lg font-medium text-xs transition-colors ${
                isSimulating
                  ? 'bg-amber-500/20 text-amber-300 border border-amber-500/40 hover:bg-amber-500/30'
                  : 'bg-emerald-600 text-white hover:bg-emerald-500 shadow-sm'
              }`}
            >
              {isSimulating ? 'Pause Radar Pulse' : 'Simulate GPS Movement'}
            </button>

            <button
              type="button"
              onClick={() => {
                const next = Math.min(1.0, currentProgress + 0.15);
                setCurrentProgress(next);
                if (onProgressUpdate) onProgressUpdate(next);
              }}
              className="px-2.5 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 border border-slate-700 font-medium"
            >
              +Step 15%
            </button>
          </div>
        )}
      </div>
    </div>
  );
};
