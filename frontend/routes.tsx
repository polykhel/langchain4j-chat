import ChatView from './views/helloworld/ChatView';
import MainLayout from 'Frontend/views/MainLayout.js';
import { createBrowserRouter, RouteObject } from 'react-router-dom';

export const routes = [
  {
    element: <MainLayout />,
    handle: { title: 'Main' },
    children: [
      { path: '/', element: <ChatView />, handle: { title: 'Ask about LangChain4j' } },
    ],
  },
] as RouteObject[];

export default createBrowserRouter(routes);
