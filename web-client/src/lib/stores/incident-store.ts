import { Incident, IncidentFilter, IncidentStatus } from "@/src/types/incidents";
import { create } from "zustand";


interface IncidentStore {
    items : Incident[];
    isLoading : boolean;
    error : string | null;

    fetchIncidents: () => Promise<void>;
    clearIncidentss: () => void;
}

export const useIncidentStore = create<IncidentStore>((set)=>({
    items:[],
    isLoading:false,
    error: null,

    fetchIncidents: async () =>{
        try {
            const response = await fetch('api/incidents');
            if(!response.ok) throw new Error('Fail get data incident');
            const data : Incident[] = await response.json();
            set({items:data, isLoading:false})
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        } catch (err:any){set({ error:err.message || 'Something went wromg', isLoading: false})}
    },
    clearIncidentss:() => set({items:[]})

}));


export const useIncidentUIStore = create<{
    filter: IncidentFilter | undefined;
    setFilter: (filter: IncidentFilter | undefined) => void;
    statusFilter: IncidentStatus | 'all';
    setStatusFilter: (status: IncidentStatus | 'all') => void;
}>((set)=>({
    statusFilter: 'all',
    setStatusFilter: (status) => set({statusFilter: status}),
    filter: undefined,
    setFilter: (filter) => set({filter})
}));