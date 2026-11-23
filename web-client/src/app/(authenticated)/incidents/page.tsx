'use client'

import { useCreateIncident, useIncidents } from "@/src/hooks/useIncidents";
import { useIncidentUIStore } from "@/src/lib/stores/incident-store";
import { CreateIncidentPayload, IncidentPriority, IncidentStatus } from "@/src/types/incidents";



export default function IncidentPage(){
// 1. Ambil state UI dari Zustand
  const statusFilter = useIncidentUIStore((state) => state.statusFilter);
  const setStatusFilter = useIncidentUIStore((state) => state.setStatusFilter);
  const filter = useIncidentUIStore((state) => state.filter);
  const setFilter = useIncidentUIStore((state) => state.setFilter);

  // 2. Ambil data API dari TanStack Query + Axios (otomatis refetch jika statusFilter berubah)
  const { data: incidents, isLoading, isError, error } = useIncidents(filter);
  const { mutate: createIncident, isPending } = useCreateIncident();

  if (isLoading) return <p>Memuat data...</p>;
  if (isError) return <p>Error: {error.message}</p>;
  const dummyIncident : CreateIncidentPayload = {
    title: 'Contoh Insiden',
    description: 'Deskripsi insiden contoh',
    category: { id: '1', title: 'Kategori Contoh' },
    status: IncidentStatus.NEW,
    priority: IncidentPriority.MEDIUM,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
    deletedAt: null,
    isDeleted: false,
    ticket_number: 'TICKET-001',

  };
    return (
  <div>
      {/* Kontrol Filter UI */}
      <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value as  IncidentStatus | 'all')}>
        <option value="all">Semua</option>
        <option value="new">New</option>
        <option value="assigned">Assigned</option>
        <option value="in_progress">In Progress</option>
        <option value="pending">Pending</option>
        <option value="resolved">Resolved</option>
        <option value="canceled">Canceled</option>
      </select>

      {/* Tombol Mutasi */}
      <button
        disabled={isPending}
        onClick={() => createIncident(dummyIncident)}
      >
        {isPending ? 'Menyimpan...' : 'Buat Insiden'}
      </button>

      {/* Render Data */}
      <ul>
        {(incidents && [])?.map((item) => (
          <li key={item.id}>
            {item?.title} — <strong>{item.status}</strong>
          </li>
        ))}
      </ul>
    </div>
    );
}