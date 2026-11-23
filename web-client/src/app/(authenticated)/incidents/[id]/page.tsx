import { useIncidentUIStore } from "@/src/lib/stores/incident-store";



export default function IncidentPage(props: { params: { id: string } }) {
  // 1. Ambil state UI dari Zustand
  const statusFilter = useIncidentUIStore((state) => state.statusFilter);
  const setStatusFilter = useIncidentUIStore((state) => state.setStatusFilter);
  const filter = useIncidentUIStore((state) => state.filter);
  const setFilter = useIncidentUIStore((state) => state.setFilter);

  return (
    <div>
      <h1>Detail Insiden</h1>
      <p>ID Insiden: {props.params.id}</p>
      <p>Status Filter: {statusFilter}</p>
      <p>Filter: {JSON.stringify(filter)}</p>
      </div>
  );
}