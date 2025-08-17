import { callFetchAccount } from "@/config/api";
import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";

// First, create the thunk
export const fetchAccount = createAsyncThunk(
  "account/fetchAccount",
  async () => {
    const response = await callFetchAccount();
    return response.data;
  }
);

interface IState {
  isAuthenticated: boolean;
  isLoading: boolean;
  isRefreshToken: boolean;
  errorRefreshToken: string;
  user: {
    _id: string;
    email: string;
    name: string;
    age?: number;
    address?: string;
    role: {
      _id: string;
      name: string;
    };
    permissions: {
      _id: string;
      name: string;
      apiPath: string;
      method: string;
      module: string;
    }[];
  };
  activeMenu: string;
}

const initialState: IState = {
  isAuthenticated: false,
  isLoading: true,
  isRefreshToken: false,
  errorRefreshToken: "",
  user: {
    _id: "",
    email: "",
    name: "",
    age: undefined,
    address: undefined,
    role: {
      _id: "",
      name: "",
    },
    permissions: [],
  },

  activeMenu: "home",
};

export const accountSlide = createSlice({
  name: "account",
  initialState,
  // The `reducers` field lets us define reducers and generate associated actions
  reducers: {
    // Use the PayloadAction type to declare the contents of `action.payload`
    setActiveMenu: (state, action) => {
      state.activeMenu = action.payload;
    },
    setUserLoginInfo: (state, action) => {
      state.isAuthenticated = true;
      state.isLoading = false;
      state.user._id = action?.payload?._id;
      state.user.email = action.payload.email;
      state.user.name = action.payload.name;
      state.user.age = action.payload.age;
      state.user.address = action.payload.address;
      state.user.role = action?.payload?.role;
      state.user.permissions = action?.payload?.permissions;
    },
    setLogoutAction: (state, action) => {
      localStorage.removeItem("access_token");
      state.isAuthenticated = false;
      state.user = {
        _id: "",
        email: "",
        name: "",
        role: {
          _id: "",
          name: "",
        },
        permissions: [],
      };
    },
    setRefreshTokenAction: (state, action) => {
      state.isRefreshToken = action.payload?.status ?? false;
      state.errorRefreshToken = action.payload?.message ?? "";
    },
    setGuest: (state) => {
      state.isAuthenticated = false;
      state.isLoading = false;
      state.user = {
        _id: "",
        email: "",
        name: "",
        role: { _id: "", name: "" },
        permissions: [],
      };
    },
  },
  extraReducers: (builder) => {
    // Add reducers for additional action types here, and handle loading state as needed
    builder.addCase(fetchAccount.pending, (state, action) => {
      state.isLoading = true;
    });

    builder.addCase(fetchAccount.fulfilled, (state, action) => {
      const payload: any = action.payload;
      if (payload && payload.user) {
        state.isAuthenticated = true;
        state.user._id = payload.user?._id || "";
        state.user.email = payload.user?.email || "";
        state.user.name = payload.user?.name || "";
        state.user.age = payload.user?.age;
        state.user.address = payload.user?.address;
        state.user.role = payload.user?.role || { _id: "", name: "" };
        state.user.permissions = payload.user?.permissions || [];
      } else {
        state.isAuthenticated = false;
        state.user = {
          _id: "",
          email: "",
          name: "",
          age: undefined,
          address: undefined,
          role: { _id: "", name: "" },
          permissions: [],
        };
      }
      state.isLoading = false;
    });

    builder.addCase(fetchAccount.rejected, (state, action) => {
      state.isAuthenticated = false;
      state.isLoading = false;
    });
  },
});

export const {
  setActiveMenu,
  setUserLoginInfo,
  setLogoutAction,
  setRefreshTokenAction,
  setGuest,
} = accountSlide.actions;

export default accountSlide.reducer;
