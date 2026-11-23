import { api } from '../lib/axios';
import { CreateIncidentPayload, Incident, IncidentFilter, IncidentStatus, UpdateIncidentPayload } from './../types/incidents';



export const incidentService = {
    getAll: async (filter?: IncidentFilter):Promise<Incident[]> =>{
        const{data } = await api.get<Incident[]>('/api/incidents',{
            params: filter ? { ...filter } : undefined,
        });
        return data;
    },
    getById: async(id:string):Promise<Incident> => {
        const { data } = await api.get<Incident>(`/api/incidents/${id}`);
        return data;
    },
    create: async (payload :CreateIncidentPayload) : Promise<Incident> => {
        const { data } = await api.post<Incident>(`/api/incidents`, payload);
        return data;
    },
    update: async (id:string, payload : UpdateIncidentPayload) : Promise<Incident> => {
        const { data } = await api.patch<Incident>(`/api/incidents/${id}`, payload);
        return data;
    },
    updateStatus : async (id: string, status : IncidentStatus) :Promise<Incident> =>{
        const { data} = await api.patch<Incident>("/api/incidents", status);
        return data;
    },
    updateAssignedTo : async (id:string, userId:string):Promise<Incident> =>{
        const {data} = await api.patch<Incident>(`/api/incidents/${id}`, userId);
        return data;
    },
    softDelete : async (id:string):Promise<void> =>{
        await api.delete(`/api/incidents/${id}`);
    },
    forceDelete : async (id:string):Promise<void> =>{
        await api.delete(`/api/incidents/${id}/force-delete`);
    }
}